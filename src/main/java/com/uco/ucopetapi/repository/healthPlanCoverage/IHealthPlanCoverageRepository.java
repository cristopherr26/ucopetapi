package com.uco.ucopetapi.repository.healthPlanCoverage;

import com.uco.ucopetapi.domain.healthPlanCoverage.HealthPlanCoverageDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IHealthPlanCoverageRepository extends JpaRepository<HealthPlanCoverageDomain, UUID> {

    // Trae los beneficios que están vivos, no se han "eliminado"
    List<HealthPlanCoverageDomain> findByHealthPlanIdAndDeletedFalse(UUID healthPlanId);

    // Evita duplicados del mismo beneficio dentro de un mismo plan
    Optional<HealthPlanCoverageDomain>
    findByIdAndHealthPlanIdAndDeletedFalse(
            UUID id,
            UUID healthPlanId
    );

    // Evita duplicados al crear una nueva cobertura
    boolean existsByHealthPlanIdAndServiceIdAndDeletedFalse(
            UUID healthPlanId,
            UUID serviceId
    );

    // Evita duplicados al actualizar una cobertura
    boolean existsByHealthPlanIdAndServiceIdAndIdNotAndDeletedFalse(
            UUID healthPlanId,
            UUID serviceId,
            UUID id
    );
}
