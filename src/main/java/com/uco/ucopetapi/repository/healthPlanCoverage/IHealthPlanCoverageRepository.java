package com.uco.ucopetapi.repository.healthPlanCoverage;

import com.uco.ucopetapi.domain.healthPlanCoverage.HealthPlanCoverageDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IHealthPlanCoverageRepository extends JpaRepository<HealthPlanCoverageDomain, UUID> {

    List<HealthPlanCoverageDomain> findByHealthPlanIdAndDeletedFalse(UUID healthPlanId);

    Optional<HealthPlanCoverageDomain>
    findByIdAndHealthPlanIdAndDeletedFalse(
            UUID id,
            UUID healthPlanId
    );

    boolean existsByHealthPlanIdAndProcedureIdAndDeletedFalse(
            UUID healthPlanId,
            UUID procedureId
    );

    boolean existsByHealthPlanIdAndProcedureIdAndIdNotAndDeletedFalse(
            UUID healthPlanId,
            UUID procedureId,
            UUID id
    );
}
