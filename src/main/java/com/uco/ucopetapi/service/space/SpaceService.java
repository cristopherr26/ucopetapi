package com.uco.ucopetapi.service.space;

import com.uco.ucopetapi.domain.space.SpaceDomain;
import com.uco.ucopetapi.repository.space.ISpaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SpaceService {

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

        // - VALIDATE BLANK STRINGS -
        if (code.isBlank() || type.isBlank() || description.isBlank()) {
            throw new IllegalArgumentException("Ningún campo de texto puede estar vacío o en blanco.");
        }

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

    @Transactional
    public Optional<SpaceDomain> changeStatus(UUID id, Boolean status) {
        Optional<SpaceDomain> existingSpace = spaceRepository.findById(id);

        if (existingSpace.isEmpty()) {
            return Optional.empty();
        }

        SpaceDomain updatedSpace = existingSpace.get();
        updatedSpace.setActive(status);

        return Optional.of(spaceRepository.save(updatedSpace));
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