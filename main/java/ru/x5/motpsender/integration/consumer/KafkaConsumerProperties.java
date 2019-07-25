package ru.x5.motpsender.integration.consumer;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.adapter.ReplyHeadersConfigurer;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import ru.x5.motpsender.dao.SessionInfo;
import ru.x5.motpsender.integration.dto.KafkaSessionInfo;

import java.util.Collections;
import java.util.Map;

/**
 * Класс описывающий конфигурацию Kafka Consumer, а также реализующий доступ к значениям из kafka.properties
 * через поля (для последующего использования их в аннотациях)
 */
@Configuration
@PropertySource("classpath:kafka.properties")
@Getter
public class KafkaConsumerProperties {

    @Value("${kafka.topic.cis.aggregated.in}")
    private String aggregatedCisInTopic;

    @Value("${kafka.topic.cis.aggregated.out}")
    private String aggregatedCisOutTopic;

    @Value("${kafka.topic.cis.status.in}")
    private String cisStatusInTopic;

    @Value("${kafka.topic.cis.status.out}")
    private String cisStatusOutTopic;

    @Value("${kafka.topic.token.in}")
    private String tokenIn;

    @Value("${kafka.topic.token.out}")
    private String tokenOut;

    @Value("${kafka.topic.products.in}")
    private String productsIn;

    @Value("${kafka.topic.products.out}")
    private String productsOut;

    @Value("${kafka.topic.repeat.in}")
    private String repeatIn;

    @Value("${kafka.repeat.group.id}")
    private String repeaterId;

    @Value("${kafka.retry.backOffPeriod}")
    private Long backOffPeriod;

    @Value("${kafka.retry.maxAttempts}")
    private int maxAttempts;

    /**
     * @return политика повторной обработки сообщений Kafka при возникновении ошибок
     */
    @Bean
    @Qualifier("kafkaRetryTemplate")
    RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(backOffPeriod);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(maxAttempts);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, KafkaSessionInfo> kafkaListenerContainerFactory(
            SessionInfo sessionInfo, ConsumerFactory consumerFactory, KafkaTemplate kafkaTemplate, KafkaMotpListenerErrorHandler kafkaMotpListenerErrorHandler
            , RetryTemplate kafkaRetryTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, KafkaSessionInfo> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setReplyTemplate(kafkaTemplate);
        factory.getContainerProperties().setAckOnError(false);
        factory.setErrorHandler(kafkaMotpListenerErrorHandler);
        factory.setRetryTemplate(kafkaRetryTemplate);
        //добавление key во все @SendTo ответы
        //todo: надо переделать, так как не всегда UUID это key
        factory.setReplyHeadersConfigurer(new ReplyHeadersConfigurer() {
            @Override
            public boolean shouldCopy(String headerName, Object headerValue) {
                return false;
            }

            @Override
            public Map<String, Object> additionalHeaders() {
                return Collections.singletonMap(KafkaHeaders.MESSAGE_KEY, sessionInfo.getGlobalUUID().toString());
            }
        });
        return factory;
    }

}
