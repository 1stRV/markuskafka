package ru.x5.motpsender.dao.dto.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import ru.x5.motpsender.dao.dto.AggregatedCisResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Используется для десериализации нестандартного представления от ИС МОТП. Имена полей используются как значения КИЗ
 * Для обмена через Kafka используется обычная десериализация
 */
public class AggregatedCisResponseDeserializer extends JsonDeserializer<AggregatedCisResponse> {

    private static final String ROOT_CIS = "rootCis";
    private static final String AGGREGATED_CIS = "aggregatedCis";

    @Override
    public AggregatedCisResponse deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String rootCisName = jp.nextFieldName();
        JsonNode node = jp.getCodec().readTree(jp);
        AggregatedCisResponse resultResponse = null;
        List<String> aggregatedCisList = new ArrayList<>();

        if (ROOT_CIS.equals(rootCisName)) {
            for (JsonNode jsonNode : node.get(AGGREGATED_CIS)) {
                aggregatedCisList.add(jsonNode.asText());
            }
            resultResponse = AggregatedCisResponse.builder().rootCis(node.get(ROOT_CIS).asText()).aggregatedCis(aggregatedCisList).build();
        } else if (rootCisName != null) {
            for (JsonNode jsonNode : node.get(rootCisName)) {
                aggregatedCisList.add(jsonNode.asText());
            }
            resultResponse = new AggregatedCisResponse(rootCisName, aggregatedCisList);
        }
        return resultResponse;
    }
}

