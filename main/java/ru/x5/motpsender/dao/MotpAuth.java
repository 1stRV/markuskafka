package ru.x5.motpsender.dao;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import ru.x5.motpsender.dao.dto.response.AuthResponse;
import ru.x5.motpsender.dao.dto.request.TokenRequest;
import ru.x5.motpsender.dao.dto.response.TokenResponse;
import ru.x5.motpsender.dao.utilites.DataSigner;
import ru.x5.motpsender.dao.utilites.SignerException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Класс содержит методы для авторизации в ИС МОТП
 */
@Log4j2
@Service
//TODO: Возвращать null плохо https://jira.x5.ru/browse/MRKS-34
public class MotpAuth {

    @Value("${motp.api.auth}")
    private String authPath;

    @Value("${motp.api.sign}")
    private String signPath;

    @Autowired
    @Qualifier("restTemplate")
    private RestTemplate restTemplate;

    @Autowired
    DataSigner dataSigner;

    /**
     * Получение данных для последующей авторизации
     *
     * @return данные для следующего подписания и авторизации
     */
    public AuthResponse getAuthCode() {
        try {
            return restTemplate.getForObject(new URI(authPath), AuthResponse.class);
        } catch (HttpServerErrorException e) {
            log.warn(e.getMessage());
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * Метод получения токена для работы с ИС МОТП
     *
     * @return bearer токен для дальнейшей работы
     */
    public TokenResponse getToken(String signerId) throws SignerException {
        AuthResponse authResponse = getAuthCode();
        String signedData = dataSigner.signData(authResponse.getData(), signerId);
        TokenRequest tokenRequest = TokenRequest.builder().uuid(authResponse.getUuid()).data(signedData).build();
        try {
            return restTemplate.postForObject(new URI(signPath), tokenRequest, TokenResponse.class);
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
