package ru.x5.motpsender.dao.dto.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

//todo: API contains only one document type. Need to clarify
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum EnumDocumentType {
    RECEIPT ("receipt");

    private String documentType;

    EnumDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @JsonCreator
    public static EnumDocumentType findByDescription(String value) {
        return EnumDocumentType.valueOf(value.toUpperCase());
    }
}
