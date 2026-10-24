package com.uco.ucopetapi.dto.person;

import java.util.UUID;

public record PersonMessageDTO(
        UUID id,
        DocumentType documentType,
        String documentNumber,
        String firstName,
        String lastName,
        String email,
        String address,
        String phone,
        Boolean admin,
        Boolean active) {

    public static PersonMessageDTO onlyId(UUID id) {
        return new PersonMessageDTO(id, null, null, null, null, null, null, null, null, null);
    }
}