package ru.x5.motpsender.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KafkaCisStatusRequest extends KafkaSessionInfo {
    private String cis;
}
