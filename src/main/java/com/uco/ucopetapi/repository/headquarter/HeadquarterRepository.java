package com.uco.ucopetapi.repository.headquarter;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HeadquarterRepository extends JpaRepository<HeadquarterDomain, UUID> {
}
