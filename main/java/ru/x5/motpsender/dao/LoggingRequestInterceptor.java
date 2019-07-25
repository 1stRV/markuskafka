package ru.x5.motpsender.dao;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;


/**
 * todo: добавлено для примера. Необходима переработка
 */
@Log4j2
@Component
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        traceResponse(response);
        return response;
    }

    private void traceRequest(HttpRequest request, byte[] body) {
        log.debug("===========================request begin================================================");
        log.debug("URI         : {}", request.getURI());
        log.debug("Method      : {}", request.getMethod());
        log.trace("Headers     : {}", request.getHeaders());
        log.trace("Request body: {}", new String(body));
        log.debug("==========================request end================================================");
    }

    private void traceResponse(ClientHttpResponse response) throws IOException {
        InputStream body = response.getBody();
        log.debug("============================response begin==========================================");
        log.debug("Status code  : {}", response.getStatusCode());
        log.trace("Status text  : {}", response.getStatusText());
        log.trace("Headers      : {}", response.getHeaders());
        if (body.markSupported()) {
            body.mark(0);
            String responseBody = IOUtils.toString(body, StandardCharsets.UTF_8);
            log.debug("Response body: {}", responseBody);
            body.reset();
        }
        log.debug("=======================response end=================================================");
    }

}
