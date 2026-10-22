package com.uco.ucopetapi.repository.healthPlan;

import com.uco.ucopetapi.domain.healthPlan.HealthPlanDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IHealthPlanRepository extends JpaRepository<HealthPlanDomain, UUID> {

    // Trae los planes que están vivos, no se han "eliminado"
    List<HealthPlanDomain> findByDeletedFalse();

    // Trae los planes que están vivos, no se han "eliminado" por ID
    Optional<HealthPlanDomain> findByIdAndDeletedFalse(UUID id);

    // Trae lo que el usuario busque indiferente de si es por ejemplo: oro - ORO / SURA - sura
    List<HealthPlanDomain> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

    List<HealthPlanDomain> findByInsuranceCompanyIgnoreCaseAndDeletedFalse(String insuranceCompany);

}