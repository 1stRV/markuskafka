package ru.x5.motpsender.dao.dto.request;

import lombok.Builder;
import lombok.Data;
import ru.x5.motpsender.dao.dto.response.AuthResponse;

import java.util.UUID;


/**
* Подлписанные данные для авторизации в ИС МОТП из
* {@link AuthResponse} подписывается с помощью ЭЦП
*/

@Data
@Builder
public class TokenRequest {
    private UUID uuid;
    private String data;
}
