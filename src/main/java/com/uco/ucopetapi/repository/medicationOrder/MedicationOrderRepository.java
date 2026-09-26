package com.uco.ucopetapi.repository.medicationOrder;

import com.uco.ucopetapi.domain.medicationOrder.MedicationOrderDomain;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicationOrderRepository extends JpaRepository<MedicationOrderDomain, UUID> {

    @EntityGraph(attributePaths = "items")
    List<MedicationOrderDomain> findByPetCareIdOrderBySignedAtDesc(UUID petCareId);

    @EntityGraph(attributePaths = "items")
    List<MedicationOrderDomain> findByPetIdOrderBySignedAtDesc(UUID petId);

    @EntityGraph(attributePaths = "items")
    Optional<MedicationOrderDomain> findWithItemsById(UUID id);
}
