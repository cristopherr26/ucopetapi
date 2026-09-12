package com.uco.ucopetapi.repository.pet;

import com.uco.ucopetapi.domain.pet.PetDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface IPetRepository extends JpaRepository<PetDomain, UUID> {

    @Query("SELECT p FROM PetDomain p WHERE p.tutorId = :tutorId AND p.headquarterId = :headquarterId AND p.isActive = true")
    List<PetDomain> findByTutorId(@Param("tutorId") UUID tutorId, @Param("headquarterId") UUID headquarterId);

    @Query("SELECT p FROM PetDomain p WHERE "
            + "p.headquarterId = :headquarterId AND "
            + "(:name IS NULL OR p.name LIKE %:name%) AND "
            + "(:breed IS NULL OR p.breed = :breed) AND "
            + "(:species IS NULL OR p.species = :species) AND "
            + "(:isActive IS NULL OR p.isActive = :isActive)")
    List<PetDomain> findByFilters(@Param("headquarterId") UUID headquarterId,
                                  @Param("name") String name,
                                  @Param("breed") String breed,
                                  @Param("species") String species,
                                  @Param("isActive") Boolean isActive);

    PetDomain findByPolicyId(UUID policyId);
}
