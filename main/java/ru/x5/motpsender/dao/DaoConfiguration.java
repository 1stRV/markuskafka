package ru.x5.motpsender.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.client.ClientHttpRequestFactorySupplier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;

@SpringBootConfiguration
@ComponentScan
@EnableRedisRepositories(basePackages = "ru.x5.motpsender.dao")
public class DaoConfiguration {

    @Bean
    @Qualifier("restTemplate")
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder, LoggingRequestInterceptor loggingRequestInterceptor) {
        return restTemplateBuilder
                .additionalInterceptors(loggingRequestInterceptor)
                .additionalMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .build();
    }

    @Bean
    @Qualifier("authRestTemplate")
    public RestTemplate authRestTemplate(RestTemplateBuilder restTemplateBuilder, LoggingRequestInterceptor loggingRequestInterceptor,
                                         TokenizedRequestInterceptor tokenizedRequestInterceptor, RestTemplateResponseErrorHandler restTemplateResponseErrorHandler) {
        return restTemplateBuilder
                .additionalInterceptors(tokenizedRequestInterceptor, loggingRequestInterceptor)
                .errorHandler(restTemplateResponseErrorHandler)
                .requestFactory(new ClientHttpRequestFactorySupplier())
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
