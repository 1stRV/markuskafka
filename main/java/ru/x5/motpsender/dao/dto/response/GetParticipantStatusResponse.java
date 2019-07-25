package ru.x5.motpsender.dao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * статусе регистрации в ИС МОТП, запрошенного ИНН
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetParticipantStatusResponse {
    private String inn;
    private String name;
    private String chief;
    private String role;
}
