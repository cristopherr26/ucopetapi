package com.uco.ucopetapi.repository.healthplancoverage;

import com.uco.ucopetapi.domain.healthplancoverage.HealthPlanCoverageDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IHealthPlanCoverageRepository
        extends JpaRepository<HealthPlanCoverageDomain, UUID> {

    List<HealthPlanCoverageDomain> findByHealthPlanId(UUID healthPlanId);

    boolean existsByHealthPlanIdAndProcedureId(
            UUID healthPlanId,
            UUID serviceId
    );
}
