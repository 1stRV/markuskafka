package ru.x5.motpsender.dao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * В результате успешного выполнения запроса в ответе вернется список продукции.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetProductListResponse {
    private List<ProductDto> productDtoList;
}
