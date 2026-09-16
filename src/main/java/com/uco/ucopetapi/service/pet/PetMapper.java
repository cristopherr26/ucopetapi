package com.uco.ucopetapi.service.pet;

import com.uco.ucopetapi.domain.pet.PetDomain;
import com.uco.ucopetapi.dto.pet.PetDTO;
import org.springframework.stereotype.Component;

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
}