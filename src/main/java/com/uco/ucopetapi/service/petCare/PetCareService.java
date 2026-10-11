package com.uco.ucopetapi.service.petCare;

import com.uco.ucopetapi.dto.petCare.PetCareDto;
import com.uco.ucopetapi.dto.petCare.PetCareStatus;


import java.util.List;
import java.util.UUID;

public interface PetCareService {

    List<PetCareDto> findAll();

    PetCareDto findById (UUID id);

    List<PetCareDto> findByFilter(UUID episodeId, PetCareStatus petCareStatus, String description);

    PetCareDto create(PetCareDto request);

    PetCareDto update(UUID id, PetCareDto request);

    PetCareDto complete(UUID petCareId, UUID requestingDoctorId, String summary);

}
