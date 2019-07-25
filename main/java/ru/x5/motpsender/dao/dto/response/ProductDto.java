package ru.x5.motpsender.dao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Экземпляр продукции, содержащий id – идентификатор продукта, gtin - международный товарный идентификатор и producerINN – ИНН производителя.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    @JsonProperty("GTIN")
    private String gtin;
    private String productName;
    private String productType;
    private String producerInn;

}
