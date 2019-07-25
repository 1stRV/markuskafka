package ru.x5.motpsender.dao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * список кодов маркировки их статус и владелец на момент запроса
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CisStatusResponse {
    private List<CisStatusDto> cisStatusDtoList;
}
