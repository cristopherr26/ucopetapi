package com.uco.ucopetapi.repository.healthplan;

import com.uco.ucopetapi.domain.healthplan.HealthPlanDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IHealthPlanRepository extends JpaRepository<HealthPlanDomain, UUID> {

    List<HealthPlanDomain> findByDeletedFalse();

    Optional<HealthPlanDomain> findByIdAndDeletedFalse(UUID id);

    List<HealthPlanDomain> findByNameContainingIgnoreCaseAndDeletedFalse(String name);

    List<HealthPlanDomain> findByInsuranceCompanyIgnoreCaseAndDeletedFalse(String insuranceCompany);

}