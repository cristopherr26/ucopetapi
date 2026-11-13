package com.uco.ucopetapi.repository.healthPlanCoverage;

import com.uco.ucopetapi.domain.healthPlanCoverage.HealthPlanCoverageDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IHealthPlanCoverageRepository extends JpaRepository<HealthPlanCoverageDomain, UUID> {

    // Trae las coberturas activas del plan
    List<HealthPlanCoverageDomain> findByHealthPlan_IdAndDeletedFalse(UUID healthPlanId);

    // Busca una cobertura específica activa
    Optional<HealthPlanCoverageDomain> findByIdAndHealthPlan_IdAndDeletedFalse(
            UUID id,
            UUID healthPlanId
    );

    // Valida duplicados al crear (solo revisa los que no están eliminados lógicamente)
    boolean existsByHealthPlan_IdAndServiceIdAndDeletedFalse(
            UUID healthPlanId,
            UUID serviceId
    );

    // Valida duplicados al actualizar excluyendo el registro actual
    boolean existsByHealthPlan_IdAndServiceIdAndIdNotAndDeletedFalse(
            UUID healthPlanId,
            UUID serviceId,
            UUID id
    );
}