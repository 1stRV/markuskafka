package ru.x5.motpsender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import ru.x5.motpsender.dao.MotpAuth;
import ru.x5.motpsender.dao.dto.response.TokenResponse;
import ru.x5.motpsender.dao.redis.MotpToken;
import ru.x5.motpsender.dao.redis.TokenRepository;
import ru.x5.motpsender.dao.utilites.SignerException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с токенами авторизации ИС МОТП
 */
@Service
@Slf4j
public class MotpTokenService {

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    MotpAuth motpAuth;

    @Autowired
    ObjectMapper objectMapper;

    @Value("${motp.api.default.token.lifetime.hours}")
    private Integer defaultTokenLifeTime;

    @Value("${motp.api.reserve.token.lifetime.hours}")
    private Integer reserveTokenLifeTime;

    @Value("${motp.encoding}")
    private String encoding;

    @Value("classpath:json/motpTokenInit.json")
    private Resource motpTokenInit;


    /**
     * Метод получения токена авторизации ИС МОТП и сохранение его в распределенный КЭШ
     *
     * @param inn ИНН юрлица для которого запрашивается токен
     *            Запрос должен вызываться при возникновении ошибки 401 в authorizedRestTemplate
     */
    public void updateToken(String inn) {
        log.debug("Refresh token for inn {}", inn);
        Optional<MotpToken> optionalMotpToken = tokenRepository.findById(inn);
        MotpToken motpToken;
        try {
            motpToken = optionalMotpToken.orElseThrow(NoSuchInnException::new);
            if (motpToken.getSignerId() == null) throw new NoSuchInnException();
        } catch (NoSuchInnException e) {
            log.error("Inn {} no found in token repository", inn);
            return;
        }
        try {
            TokenResponse tokenResponse = motpAuth.getToken(motpToken.getSignerId());
            motpToken.setToken(tokenResponse.getToken())
                    .setTokenDateTime(LocalDateTime.now())
                    .setLifetime(tokenResponse.getLifetime());
        } catch (SignerException e) {
            log.error("Error during get token for inn {} with cause {}", inn, e.getMessage());
            return;
        }
        tokenRepository.save(motpToken);
        log.debug("Token for inn {} refreshed", inn);
    }

    /**
     * Метод для первичной инициализации хранилища Redis.
     * Предназначен для загрузки ИНН и связки их с табельным номером подписанта для SAP PI
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initRedisData() {
        log.trace("Start init redis data");
        List<MotpToken> motpTokenList;
        try {
            String motpTokenInitJson = IOUtils.toString(motpTokenInit.getInputStream(), encoding);
            motpTokenList = objectMapper.readValue(motpTokenInitJson, objectMapper.getTypeFactory().constructCollectionType(List.class, MotpToken.class));
        } catch (IOException e) {
            log.error("Error when init redis data. {}", e.getCause());
            return;
        }
        for (MotpToken motpToken : motpTokenList) {
            if (!tokenRepository.existsById(motpToken.getInn())) {
                tokenRepository.save(motpToken);
                log.debug("New INN {} added to redis when started", motpToken.getInn());
            } else {
                MotpToken motpTokenForChange = null;
                try {
                    motpTokenForChange = tokenRepository.findById(motpToken.getInn()).orElseThrow(NoSuchInnException::new);
                } catch (NoSuchInnException e) {
                    log.error("Inn {} no found in token repository", motpToken.getInn());
                }
                motpTokenForChange.setSignerId(motpToken.getSignerId());
                tokenRepository.save(motpTokenForChange);
            }
        }


        log.trace("Finish init redis data");
    }


    /**
     * Задание для получения токена. Производится проверка условия истечения времени жизни токена
     * В целях тестирования запрос токена происходит по cron
     */
    @Scheduled(cron = "${motp.auth.refresh.cron}")
    public void refreshToken() {
        log.trace("Start getting tokens job");
        Iterable<MotpToken> motpTokenList = tokenRepository.findAll();
        for (MotpToken motpToken : motpTokenList) {
            if (motpToken != null && (motpToken.getTokenDateTime() == null ||
                    (motpToken.getLifetime() == null &&
                            motpToken.getTokenDateTime().plusHours(defaultTokenLifeTime).isBefore(LocalDateTime.now())) ||
                    (motpToken.getLifetime() != null &&
                            motpToken.getTokenDateTime().plusMinutes(motpToken.getLifetime())
                                    .isAfter(LocalDateTime.now().minusHours(reserveTokenLifeTime))))) {
                try {
                    log.debug("Start getting token job for inn: {}", motpToken.getInn());
                    updateToken(motpToken.getInn());
                    log.debug("End getting token job for inn: {}", motpToken.getInn());
                } catch (ResourceAccessException e) {
                    log.error("Thanks to the vigilance of the PEB, or the error in the URL, access to the MOTP is impossible", e.getCause());
                } catch (Exception e) {
                    log.error("Error while job getting token for INN {}", motpToken.getInn(), e.getCause());
                }
            }
        }
        log.trace("End getting tokens job");
    }
}
