package ru.x5.motpsender.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Описание сообщения kafka для получения списка продуктов
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaProductListRequest extends KafkaSessionInfo{
    private Integer limit;
}
