package com.uco.ucopetapi.repository.VitalSigns;

import com.uco.ucopetapi.domain.vitalSigns.VitalSignsDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VitalSignsRepository extends JpaRepository <VitalSignsDomain, UUID>{
}
