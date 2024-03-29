package ru.x5.motpsender.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Описание сообщения kafka для его повторной обработки
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaRepeatRequest extends KafkaSessionInfo{
    private Long offset;
    private String topic;
    private int partition = 0;
    private String key;
}
