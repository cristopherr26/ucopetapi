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
        Boolean active = (space.getActive() != null) ? space.getActive() : true;

        SpaceDomain newSpace = new SpaceDomain(
                UUID.randomUUID(),
                space.getCode(),
                space.getType(),
                space.getDescription(),
                active
        );
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