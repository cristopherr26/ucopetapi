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
        boolean admin,
        boolean active) {

    public static PersonMessageDTO onlyId(UUID id) {
        // Para dar de baja el procesador solo lee el id.
        return new PersonMessageDTO(id, null, null, null, null, null, null, null, false, false);
    }
}