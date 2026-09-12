package com.uco.ucopetapi.repository.specialtie;

import com.uco.ucopetapi.domain.specialtie.HeadquarterDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HeadquarterRepository extends JpaRepository<HeadquarterDomain, UUID> {
}
