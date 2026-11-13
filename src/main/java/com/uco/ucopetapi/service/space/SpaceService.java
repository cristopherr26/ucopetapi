package com.uco.ucopetapi.service.space;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.space.SpaceDomain;
import com.uco.ucopetapi.messaging.spaces.MessageSenderBrokerCreationSpace;
import com.uco.ucopetapi.messaging.spaces.MessageSenderBrokerDeactivationSpace;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
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
    private final HeadquarterRepository headquarterRepository;
    private final MessageSenderBrokerCreationSpace creationSender;
    private final MessageSenderBrokerDeactivationSpace deactivationSender;

    public SpaceService(ISpaceRepository spaceRepository,
                        HeadquarterRepository headquarterRepository,
                        MessageSenderBrokerCreationSpace creationSender,
                        MessageSenderBrokerDeactivationSpace deactivationSender) {
        this.spaceRepository = spaceRepository;
        this.headquarterRepository = headquarterRepository;
        this.creationSender = creationSender;
        this.deactivationSender = deactivationSender;
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

        // - VALIDATE HEADQUARTER (SEDE): obligatoria, existente y activa -
        UUID headquarterId = validateHeadquarter(space.getHeadquarterId());

        // - PUBLISH (async): el procesador de Salud es quien guarda en la BD -
        // El id se genera aqui para poder devolverlo al cliente en el 202.
        SpaceDomain newSpace = new SpaceDomain(UUID.randomUUID(), code, type, description, active, headquarterId);
        creationSender.execute(newSpace, newSpace.getId().toString());
        return newSpace;
    }

    // PUT: actualiza la descripcion. Nada se guarda aqui: se publica el cambio (cola de creacion/actualizacion)
    // y se responde con como va a quedar el espacio. La entidad que trae Hibernate NO se modifica.
    public Optional<SpaceDomain> updateSpace(UUID id, SpaceDomain space) {
        Optional<SpaceDomain> existingSpace = spaceRepository.findById(id);

        if (existingSpace.isEmpty()) {
            return Optional.empty();
        }

        if (space.getDescription() == null) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        String description = validateDescription(space.getDescription().trim());

        SpaceDomain current = existingSpace.get();
        creationSender.execute(new SpaceDomain(id, null, null, description, null), id.toString());

        return Optional.of(new SpaceDomain(id, current.getCode(), current.getType(), description, current.getActive(), current.getHeadquarterId()));
    }

    // PATCH clave/valor = Se modifican las claves que vienen en el body.
    // Cuando la clave o valor es invalido se lanza IllegalArgumentException.
    // Nada se guarda aqui: si solo viene {"active": false} va a la cola de desactivacion;
    // cualquier otro cambio va a la cola de creacion/actualizacion.
    public Optional<SpaceDomain> patchSpace(UUID id, Map<String, Object> updates) {
        Optional<SpaceDomain> existingSpace = spaceRepository.findById(id);

        if (existingSpace.isEmpty()) {
            return Optional.empty();
        }

        if (updates == null || updates.isEmpty()) {
            throw new IllegalArgumentException("Debe enviar al menos un campo para actualizar.");
        }

        // "change" lleva SOLO lo que cambia (el resto queda en null = sin cambio)
        SpaceDomain change = new SpaceDomain(id, null, null, null, null);
        updates.forEach((key, value) -> applyChange(change, key, value));

        boolean onlyDeactivation = Boolean.FALSE.equals(change.getActive())
                && change.getType() == null
                && change.getDescription() == null;

        if (onlyDeactivation) {
            deactivationSender.execute(change, id.toString());
        } else {
            creationSender.execute(change, id.toString());
        }

        SpaceDomain current = existingSpace.get();
        return Optional.of(new SpaceDomain(
                id,
                current.getCode(),
                change.getType() != null ? change.getType() : current.getType(),
                change.getDescription() != null ? change.getDescription() : current.getDescription(),
                change.getActive() != null ? change.getActive() : current.getActive(),
                current.getHeadquarterId()));
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

    // La sede es obligatoria al crear, tiene que existir y estar activa.
    // Despues de creado, el espacio no cambia de sede (por eso el PATCH no acepta "headquarterId").
    private UUID validateHeadquarter(UUID headquarterId) {
        if (headquarterId == null) {
            throw new IllegalArgumentException("La sede es obligatoria.");
        }

        HeadquarterDomain headquarter = headquarterRepository.findById(headquarterId)
                .orElseThrow(() -> new IllegalArgumentException("La sede indicada no existe."));

        if (!Boolean.TRUE.equals(headquarter.getIsActive())) {
            throw new IllegalArgumentException("La sede indicada está inactiva.");
        }
        return headquarterId;
    }

    public boolean headquarterExists(UUID headquarterId) {
        return headquarterRepository.existsById(headquarterId);
    }

    @Transactional
    public List<SpaceDomain> getSpacesByHeadquarter(UUID headquarterId) {

        return spaceRepository.findByHeadquarterId(headquarterId);
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