package ru.x5.motpsender.integration.consumer;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;
import ru.x5.motpsender.dao.MotpSender;
import ru.x5.motpsender.dao.SessionInfo;
import ru.x5.motpsender.dao.dto.AggregatedCisResponse;
import ru.x5.motpsender.dao.dto.response.CisStatusDto;
import ru.x5.motpsender.dao.dto.response.GetProductListResponse;
import ru.x5.motpsender.dao.redis.MotpToken;
import ru.x5.motpsender.dao.redis.TokenRepository;
import ru.x5.motpsender.integration.dto.KafkaAggregatedCisRequest;
import ru.x5.motpsender.integration.dto.KafkaCisStatusRequest;
import ru.x5.motpsender.integration.dto.KafkaProductListRequest;

import java.text.MessageFormat;
import java.util.Optional;

/**
 * Класс получает сообщения от внешних систем через kafka, обрабатывает и направляет ответ
 * Класс включает в себя бизнес-логику сервиса. Возможно, это надо изменить для добавления синхронных интерфейсов
 */
@Log4j2
@Service
public class KafkaMotpConsumer {

    @Autowired
    private MotpSender motpSender;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private SessionInfo sessionInfo;

    /**
     * Введен для синхронизации при тестировании
     */
    @Getter
    volatile boolean aggregatedCisRunned;

    /**
     * Введен для синхронизации при тестировании
     */
    @Getter
    volatile boolean cisStatusRunned;

    /**
     * Введен для синхронизации при тестировании
     */
    @Getter
    volatile boolean productsRunned;

    @KafkaListener(topics = "#{kafkaConsumerProperties.getAggregatedCisInTopic()}", errorHandler = "kafkaMotpListenerErrorHandler")
    @SendTo("#{kafkaConsumerProperties.getAggregatedCisOutTopic()}")
    public AggregatedCisResponse aggregatedCis(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String keyUUID, KafkaAggregatedCisRequest message) {
        sessionInfo.setGlobalUUID(keyUUID);
        sessionInfo.setUserInn(message.getUserInn());
        AggregatedCisResponse aggregatedCisResponse = motpSender.getAggregatedCis(message.getCis());
        aggregatedCisRunned = true;
        return aggregatedCisResponse;

    }

    @KafkaListener(topics = "#{kafkaConsumerProperties.getCisStatusInTopic()}")
    @SendTo("#{kafkaConsumerProperties.getCisStatusOutTopic()}")
    public CisStatusDto getCisStatus(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String keyUUID, KafkaCisStatusRequest message) {
        sessionInfo.setGlobalUUID(keyUUID);
        sessionInfo.setUserInn(message.getUserInn());
        CisStatusDto cisStatusDto = motpSender.getCisStatus(message.getCis());
        cisStatusRunned = true;
        return cisStatusDto;
    }

    @KafkaListener(topics = "#{kafkaConsumerProperties.getTokenIn()}")
    @SendTo("#{kafkaConsumerProperties.getTokenOut()}")
    public MotpToken setToken(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String keyUUID, MotpToken message) {
        sessionInfo.setGlobalUUID(keyUUID);
        sessionInfo.setUserInn(message.getInn());
        tokenRepository.save(message);
        Optional<MotpToken> optionalMotpToken = tokenRepository.findById(message.getInn());
        if (optionalMotpToken.isPresent()) {
            return optionalMotpToken.get();
        } else {
            log.error(MessageFormat.format("Не найден токен для ИНН {0}", message.getInn()));
            return null;
        }

    }

    @KafkaListener(topics = "#{kafkaConsumerProperties.getProductsIn()}")
    @SendTo("#{kafkaConsumerProperties.getProductsOut()}")
    public GetProductListResponse products(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String keyUUID, KafkaProductListRequest message) {
        sessionInfo.setGlobalUUID(keyUUID);
        sessionInfo.setUserInn(message.getUserInn());
        GetProductListResponse getProductListResponse = motpSender.getProductsList(message.getLimit().toString());
        productsRunned = true;
        return getProductListResponse;
    }

    /**
     * Метод используется для тестирования и отладки
     * @param keyUUID
     * @param message
     */
    @KafkaListener(topics = "testTopic", errorHandler = "kafkaMotpListenerErrorHandler")
    public void test(@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String keyUUID, String message) {
        log.debug("Receive msg " + keyUUID + " lenght: " + message.length());
        if ("04f12f765-7b04-4c5f-a94e-27c51a608821".equals(keyUUID) || "0f90a9355-9265-4879-9f87-587c02a6392b".equals(keyUUID))
            throw new KafkaException("my kafka exception");
    }


}
