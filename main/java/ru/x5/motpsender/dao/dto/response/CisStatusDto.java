package ru.x5.motpsender.dao.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.x5.motpsender.dao.dto.enums.CisCodeStatus;
import ru.x5.motpsender.dao.dto.enums.EnumPackageType;

import java.math.BigDecimal;
import java.util.List;

/**
 * Информация по коду маркировки, получаемая из МОТП
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CisStatusDto {
    /**
     * Код маркировки из запроса
     */
    private String requestedCis;
    /**
     * КИЗ товара
     */
    private String cis;
    /**
     * Статус кода маркировки
     */
    private CisCodeStatus status;
    /**
     * GTIN товара
     */
    private String gtin;
    /**
     * Наименование товара
     */
    private String productName;
    /**
     *МРЦ (максимальная розничная цена)
     */
    private BigDecimal maxRetailPrice;
    /**
     * Тип упаковки
     */
    private EnumPackageType packageType;
    /**
     * Состав КИЗа (список КМ)
     */
    private List<String> child;
    /**
     * Родительская упаковка
     */
    private String parent;
    /**
     * ИНН производителя
     */
    private String producerInn;
    /**
     * Наименование производителя
     */
    private String producerName;
    /**
     * ИНН собственника КМ
     */
    private String ownerInn;
    /**
     * Наименование собственника КМ
     */
    private String ownerName;
}
