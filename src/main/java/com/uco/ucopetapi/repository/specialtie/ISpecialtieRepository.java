package com.uco.ucopetapi.repository.specialtie;

import com.uco.ucopetapi.domain.specialtie.SpecialtieDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ISpecialtieRepository extends JpaRepository<SpecialtieDomain, UUID> {

    Optional<SpecialtieDomain> findSpecialtieByName(String name);
}
