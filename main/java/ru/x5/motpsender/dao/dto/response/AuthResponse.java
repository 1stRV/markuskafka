package ru.x5.motpsender.dao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Данные для авторизации в ИС МОТП
 * data подписывается с помощью ЭЦП
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private UUID uuid;
    private String data;
}
