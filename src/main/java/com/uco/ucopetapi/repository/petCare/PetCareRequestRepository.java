package com.uco.ucopetapi.repository.petCare;

import com.uco.ucopetapi.domain.petCare.PetCareRequestDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PetCareRequestRepository extends JpaRepository<PetCareRequestDomain, UUID> {

    List<PetCareRequestDomain> findByPetCareIdOrderByRequestedAtAsc(UUID petCareId);
}
