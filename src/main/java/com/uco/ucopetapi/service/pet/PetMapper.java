package com.uco.ucopetapi.service.pet;

import com.uco.ucopetapi.domain.pet.PetDomain;
import com.uco.ucopetapi.dto.pet.PetDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PetMapper {

    public PetDTO toDTO(PetDomain pet) {

        return new PetDTO(
                pet.getId(),
                pet.getName(),
                pet.getBirthDate(),
                pet.getBreed(),
                pet.getSpecies(),
                pet.getGender(),
                pet.getPhotoUrl(),
                pet.getTutorId(),
                pet.getPolicyId(),
                pet.getHeadquarterId(),
                pet.isActive()
        );
    }

    public List<PetDTO> toDTOList(List<PetDomain> domains) {
        if (domains == null) return List.of();

        return domains.stream()
                .map(this::toDTO)
                .toList();
    }

    public PetDomain toDomain(PetDTO pet) {

        return new PetDomain(
                pet.getId(),
                pet.getName(),
                pet.getBirthDate(),
                pet.getBreed(),
                pet.getSpecies(),
                pet.getGender(),
                pet.getPhotoUrl(),
                pet.getTutorId(),
                pet.getPolicyId(),
                pet.isActive(),
                pet.getHeadquarterId()
        );
    }
}