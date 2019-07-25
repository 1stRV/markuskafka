package ru.x5.motpsender.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;
import ru.x5.motpsender.dao.dto.AggregatedCisResponse;
import ru.x5.motpsender.dao.dto.GetPartnersResponse;
import ru.x5.motpsender.dao.dto.MyCisPrepareRequest;
import ru.x5.motpsender.dao.dto.MyCisPrepareResponse;
import ru.x5.motpsender.dao.dto.enums.EnumDocumentType;
import ru.x5.motpsender.dao.dto.enums.RequestOrderStatus;
import ru.x5.motpsender.dao.dto.exception.EmptyResponseException;
import ru.x5.motpsender.dao.dto.response.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Класс для отправки запросов в ИС МОТП. Содержит основные методы запроса данных из раздела 2 API 1.2.2
 */
@Log4j2
@Service
//TODO: Возвращать null плохо https://jira.x5.ru/browse/MRKS-34
public class MotpSender {

    private static final String INVALID_URI = "Invalid uri path: {0}";
    private static final String RESPONSE_LOG_TEXT = "Request to MOTP: {0} return response: {1}";
    private static final String LIMIT_PARAM = "limit";
    public static final String EMPTY_RESPONSE = "Post request for MOTP {} return empty response";

    @Value("${motp.api.participant.status}")
    private String participantStatusPath;

    @Value("${motp.api.partners}")
    private String partnersPath;

    @Value("${motp.api.product}")
    private String productPath;

    @Value("${motp.api.product.card}")
    private String productCardPath;

    @Value("${motp.api.cis.status}")
    private String cisStatusPath;

    @Value("${motp.api.cis.status.list}")
    private String cisStatusListPath;

    @Value("${motp.api.cis.aggregated}")
    private String cisAggregatedPath;

    @Value("${motp.api.cis.my.prepare}")
    private String myCisPreparePath;

    @Value("${motp.api.documents.body}")
    private String documentBodyPath;

    @Value("${motp.api.documents.prepare}")
    private String myDocumentsPreparePath;

    @Value("${motp.api.order.status}")
    private String requestOrderStatusPath;

    @Value("${motp.api.order.result}")
    private String requestOrderResultPath;

    @Value("${motp.encoding}")
    private String motpEncoding;

    @Autowired
    @Qualifier("authRestTemplate")
    RestTemplate authRestTemplate;

    @Autowired
    ObjectMapper objectMapper;


    /**
     * 2.1.1 Запрос статуса регистрации участника
     *
     * @param participiantINN
     * @return информация о статусе регистрации в ИС МОТП, запрошенного ИНН
     */
    public GetParticipantStatusResponse getParticipiantStatus(String participiantINN) {
        try {
            String path = getUrlEncodedPath(participantStatusPath, participiantINN);
            return authRestTemplate.getForObject(new URI(path), GetParticipantStatusResponse.class);
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, participantStatusPath), e);
        }
        return null;
    }

    /**
     * todo: change for api 1.2.3 (not realized yet)
     * 2.1.2 Запрос списка контрагентов участника
     * @return список, состоящий из ИНН контрагентов
     */
    public GetPartnersResponse getPartners() {
        try {
            return authRestTemplate.getForObject(new URI(partnersPath), GetPartnersResponse.class);
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, partnersPath), e);
        }
        return null;
    }

    /**
     * 2.2.1 Запрос списка продукции
     * @param limit количество возвращаемых позиций
     * @return список продукции gtin - международный товарный идентификатор и producerINN – ИНН производителя.
     */
    public GetProductListResponse getProductsList(String limit) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(LIMIT_PARAM, limit);
        ProductDto[] productDtos = authRestTemplate.getForObject(productPath, ProductDto[].class, parameters);
        try {
            return new GetProductListResponse(Arrays.asList((Optional.of(productDtos).orElseThrow(EmptyResponseException::new))));
        } catch (EmptyResponseException e) {
            log.error(MessageFormat.format(EMPTY_RESPONSE, productPath), e);
        }
        return null;
    }

    /**
     * 2.2.2 Запрос карточки продукта
     * @param gtin
     * @return информация о продукте и его производителе.
     */
    public ProductDto getProductCard(String gtin) {
        try {
            String path = getUrlEncodedPath(productCardPath, gtin);
            return authRestTemplate.getForObject(new URI(path), ProductDto.class);
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, productCardPath), e);
        }
        return null;
    }

    /**
     * 2.3.1 Запрос данных по одному коду маркировки
     * @param cis
     * @return В результате успешного выполнения запроса в ответе вернется информация по коду маркировки.
     */
    public CisStatusDto getCisStatus(String cis) {
        String path = getUrlEncodedPath(cisStatusPath, cis);
        try {
            CisStatusDto cisStatusDto = authRestTemplate.getForObject(new URI(path), CisStatusDto.class);
            log.trace(RESPONSE_LOG_TEXT, path, cisStatusDto);
            return cisStatusDto;
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, cisStatusPath), e);
        }
        return null;
    }

    /**
     * 2.3.2 Запрос данных по списку кодов маркировки
     *
     * @param cisList
     * @return В результате успешного выполнения запроса в ответе вернется информация по кодам маркировки.
     */
    public CisStatusResponse getCisStatusList(List<String> cisList) {
        try {
            CisStatusDto[] cisStatusDtos = authRestTemplate.postForObject(new URI(cisStatusListPath), cisList, CisStatusDto[].class);
            return new CisStatusResponse(Arrays.asList(Optional.of(cisStatusDtos).orElseThrow(EmptyResponseException::new)));
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, cisStatusListPath), e);
        } catch (EmptyResponseException e) {
            log.error(MessageFormat.format(EMPTY_RESPONSE, cisStatusListPath), e);
        }
        return null;
    }

    /**
     * 2.3.3 Запрос данных об агрегации кодов маркировки
     * todo: change for api 1.2.3 (not realized yet)
     * @param cis
     * @return информация о составе кода агрегата
     */
    public AggregatedCisResponse getAggregatedCis(String cis) {
        String path = getUrlEncodedPath(cisAggregatedPath, cis);
        try {
            return authRestTemplate.getForObject(new URI(path), AggregatedCisResponse.class);
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, path));
            log.debug(e.getMessage());
        }
        return null;
    }

    //todo: realize if needed 2.3.4 Запрос цепочки движения кода маркировки


    /**
     * todo: change for api 1.2.3
     * 2.3.5 Запрос списка кодов маркировки, принадлежащих участнику
     * 1 этап:  Формирование заказа на список кодов. С необязательным фильтром указания
     * типа упаковки. Короб- box, блок- block и пачка- pack.
     *
     * @param myCisPrepareRequest
     * @return идентификатор запроса order в ИС МОТП
     */
    //todo: Need to specify result format. API not support JSON. Answer: result is String
    public MyCisPrepareResponse getMyCisPrepare(MyCisPrepareRequest myCisPrepareRequest) {
        try {
            return authRestTemplate.postForObject(new URI(myCisPreparePath), myCisPrepareRequest, MyCisPrepareResponse.class);
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, myCisPreparePath), e);
        }
        return null;
    }


    /**
     * 2.4.2 Запрос списка документов
     * Формирование заказа на список документов. С необязательным фильтром
     * указания типа документа.
     *
     * @param enumDocumentType - необязательный тип документа
     * @return
     */
    //todo: Need to specify result format. API not support JSON. Answer: result is String
    public String getMyDocumentsPrepare(EnumDocumentType enumDocumentType) {
        try {
            return authRestTemplate.postForObject(new URI(myDocumentsPreparePath), enumDocumentType, String.class);
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, myDocumentsPreparePath), e);
        }
        return null;
    }

    /**
     * 2.4.2 Запрос списка документов
     * 3 этап: Запрос результата заказа.
     *
     * @param documentId
     * @return ?
     */
    //todo: Need to specify result format and tests
    public String getDocumentBody(String documentId) {
        String path = getUrlEncodedPath(documentBodyPath, documentId);
        try {
            return authRestTemplate.getForObject(new URI(path), String.class);
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, path));
            log.debug(e.getMessage());
        }
        return null;
    }

    /**
     * Метод применим к двум процессам
     * 2.3.5 Запрос списка кодов маркировки, принадлежащих участнику
     * 2.4.2 Запрос списка документов
     * 2 этап: Проверка статуса заказа.
     *
     * @param orderUUID
     * @return В процессе выполнения- IN PROGRESS, задание выполнено- SUCCESS, при выполнении возникла ошибка- ERROR.
     */
    public RequestOrderStatus getRequestOrderStatus(String orderUUID) {
        String path = getUrlEncodedPath(requestOrderStatusPath, orderUUID);
        try {
            String result = authRestTemplate.getForObject(new URI(path), String.class);
            return result != null ? RequestOrderStatus.findByDescription(result) : null;
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, path));
            log.trace(e.getMessage());
        }
        return null;
    }

    /**
     * 2.3.5 Запрос списка кодов маркировки, принадлежащих участнику
     * 3 этап: Запрос результата заказа.
     *
     * @param orderUUID
     * @return ?
     */
    //todo: Need to specify result format and tests
    public String getRequestOrderResult(String orderUUID) {
        String path = getUrlEncodedPath(requestOrderResultPath, orderUUID);
        try {
            return authRestTemplate.getForObject(new URI(path), String.class);
        } catch (URISyntaxException e) {
            log.error(MessageFormat.format(INVALID_URI, path));
            log.trace(e.getMessage());
        }
        return null;
    }

    //todo: realize if needed 2.4.1 Запрос контента документа

    private String getUrlEncodedPath(String path, String parameter) {
        return UriUtils.encodePath(String.format(path, parameter), motpEncoding);
    }
}
