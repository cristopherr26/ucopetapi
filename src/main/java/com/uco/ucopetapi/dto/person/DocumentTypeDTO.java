package com.uco.ucopetapi.dto.person;

public record DocumentTypeDTO(String value, String label) {
    public static DocumentTypeDTO de(DocumentType tipo) {
        return new DocumentTypeDTO(tipo.name(), tipo.getDescripcion());
    }
}
