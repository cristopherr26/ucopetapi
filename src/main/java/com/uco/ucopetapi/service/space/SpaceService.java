package com.uco.ucopetapi.service.space;

import com.uco.ucopetapi.domain.space.SpaceDomain;
import com.uco.ucopetapi.repository.space.ISpaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class SpaceService {

    // Claves que el PATCH acepta
    private static final Set<String> PATCHABLE_FIELDS = Set.of("type", "description", "active");

    private final ISpaceRepository spaceRepository;

    public SpaceService(ISpaceRepository spaceRepository) {
        this.spaceRepository = spaceRepository;
    }

    public List<SpaceDomain> getAllSpaces() {
        return spaceRepository.findAll();
    }

    @Transactional
    public SpaceDomain createSpace(SpaceDomain space) {

        // - ASSIGN DEFAULT VALUES IF NULL -
        String code = space.getCode() != null ? space.getCode().trim() : "ESP-01";
        String type = space.getType() != null ? space.getType().trim() : "Peluquería";
        String description = space.getDescription() != null ? space.getDescription().trim() : "Zona de baño";
        Boolean active = Optional.ofNullable(space.getActive()).orElse(true);

        // - VALIDATE BLANK STRINGS (OBLIGATORIOS) -
        if (code.isBlank() || type.isBlank() || description.isBlank()) {
            throw new IllegalArgumentException("Ningún campo de texto (código, tipo o descripción) puede estar vacío o en blanco.");
        }

        // - VALIDATE MAXIMUM LENGTH (LONGITUD MÁXIMA) -
        if (code.length() > 10) {
            throw new IllegalArgumentException("El código no puede superar los 10 caracteres.");
        }
        validateType(type);
        validateDescription(description);

        // - VALIDATE CODE DUPLICATION -
        if (spaceRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("El código del espacio ya existe en el sistema.");
        }

        // - VALIDATE CODE FORMAT USING REGEX -
        if (!code.matches("^[A-Z]{3}-\\d+$")) {
            throw new IllegalArgumentException("El código debe tener el formato de 3 letras mayúsculas, un guion y números (Ej: CON-101).");
        }

        // - SAVE AND RETURN SPACE -
        SpaceDomain newSpace = new SpaceDomain(UUID.randomUUID(), code, type, description, active);
        return spaceRepository.save(newSpace);
    }

    @Transactional
    public Optional<SpaceDomain> updateSpace(UUID id, SpaceDomain space) {
        Optional<SpaceDomain> existingSpace = spaceRepository.findById(id);

        if (existingSpace.isEmpty()) {
            return Optional.empty();
        }

        SpaceDomain updatedSpace = existingSpace.get();
        updatedSpace.setDescription(space.getDescription());

        return Optional.of(spaceRepository.save(updatedSpace));
    }

    // PATCH clave/valor = Se modifican las claves que vienen en el body.
    // Cuando la clave o valor es invalido se lanza IllegalArgumentException
    @Transactional
    public Optional<SpaceDomain> patchSpace(UUID id, Map<String, Object> updates) {
        Optional<SpaceDomain> existingSpace = spaceRepository.findById(id);

        if (existingSpace.isEmpty()) {
            return Optional.empty();
        }

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un campo para actualizar.");
        }

        SpaceDomain space = existingSpace.get();
        updates.forEach((key, value) -> applyChange(space, key, value));

        return Optional.of(spaceRepository.save(space));
    }

    private void applyChange(SpaceDomain space, String key, Object value) {
        if (!PATCHABLE_FIELDS.contains(key)) {
            throw new IllegalArgumentException("El campo '" + key + "' no se puede modificar. Campos permitidos: " + PATCHABLE_FIELDS + ".");
        }

        switch (key) {
            case "type" -> space.setType(validateType(asText(key, value)));
            case "description" -> space.setDescription(validateDescription(asText(key, value)));
            default -> space.setActive(asBoolean(key, value));
        }
    }

    private String asText(String key, Object value) {
        if (value instanceof String text) {
            return text.trim();
        }
        throw new IllegalArgumentException("El campo '" + key + "' debe ser un texto.");
    }

    private Boolean asBoolean(String key, Object value) {
        if (value instanceof Boolean flag) {
            return flag;
        }
        throw new IllegalArgumentException("El campo '" + key + "' debe ser true o false.");
    }

    private String validateType(String type) {
        if (type.isBlank() || type.length() < 5 || type.length() > 50) {
            throw new IllegalArgumentException("El tipo de espacio debe tener entre 5 y 50 caracteres.");
        }
        return type;
    }

    private String validateDescription(String description) {
        if (description.isBlank() || description.length() < 10 || description.length() > 255) {
            throw new IllegalArgumentException("La descripción debe tener entre 10 y 255 caracteres.");
        }
        return description;
    }

    @Transactional
    public Optional<SpaceDomain> getSpaceByCode(String code) {

        return spaceRepository.findByCode(code);
    }

    @Transactional
    public List<SpaceDomain> getSpacesByType(String type) {

        return spaceRepository.findByType(type);
    }

    @Transactional
    public List<SpaceDomain> getSpacesByStatus(Boolean active) {

        return spaceRepository.findByActive(active);
    }

}