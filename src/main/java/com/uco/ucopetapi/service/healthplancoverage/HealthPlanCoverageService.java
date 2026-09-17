package com.uco.ucopetapi.service.healthplancoverage;

import com.uco.ucopetapi.domain.healthplan.HealthPlanDomain;
import com.uco.ucopetapi.domain.healthplancoverage.HealthPlanCoverageDomain;
import com.uco.ucopetapi.dto.healthplancoverage.HealthPlanCoverageDTO;
import com.uco.ucopetapi.repository.healthplan.IHealthPlanRepository;
import com.uco.ucopetapi.repository.healthplancoverage.IHealthPlanCoverageRepository;
import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class HealthPlanCoverageService {

    private final IHealthPlanCoverageRepository coverageRepository;
    private final IHealthPlanRepository healthPlanRepository;
    private final RestClient restClient;

    public HealthPlanCoverageService(
            IHealthPlanCoverageRepository coverageRepository,
            IHealthPlanRepository healthPlanRepository) {

        this.coverageRepository = coverageRepository;
        this.healthPlanRepository = healthPlanRepository;
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Transactional(readOnly = true)
    public List<HealthPlanCoverageDTO> findByHealthPlanId(
            UUID healthPlanId
    ) {

        validateHealthPlan(healthPlanId);

        return coverageRepository
                .findByHealthPlanIdAndDeletedFalse(healthPlanId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public HealthPlanCoverageDTO findById(UUID healthPlanId, UUID coverageId) {

        validateHealthPlan(healthPlanId);

        HealthPlanCoverageDomain healthPlanCoverage =
                coverageRepository
                        .findByIdAndHealthPlanIdAndDeletedFalse(
                                coverageId,
                                healthPlanId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Coverage not found"
                                )
                        );

        return toDTO(healthPlanCoverage);
    }

    public HealthPlanCoverageDTO save( UUID healthPlanId, HealthPlanCoverageDTO dto) {

        HealthPlanDomain healthPlan =
                validateHealthPlan(healthPlanId);

        validatePercentage(dto.getCoveragePercentage());
        validateProcedure(dto.getProcedureId());
        validateCoverageLimit(healthPlanId);

        boolean duplicated =
                coverageRepository
                        .existsByHealthPlanIdAndProcedureIdAndDeletedFalse(
                                healthPlanId,
                                dto.getProcedureId()
                        );

        if (duplicated) {
            throw new RuntimeException(
                    "The service already has a coverage in this health plan"
            );
        }

        HealthPlanCoverageDomain healthPlanCoverage =
                new HealthPlanCoverageDomain();

        healthPlanCoverage.setHealthPlan(healthPlan);
        healthPlanCoverage.setProcedureId(dto.getProcedureId());
        healthPlanCoverage.setCoveragePercentage(dto.getCoveragePercentage());
        healthPlanCoverage.setCoverageLimit(dto.getCoverageLimit());
        healthPlanCoverage.setDeleted(false);

        HealthPlanCoverageDomain saved =
                coverageRepository.save(healthPlanCoverage);

        return toDTO(saved);
    }

    public HealthPlanCoverageDTO update(UUID healthPlanId, UUID coverageId, HealthPlanCoverageDTO dto) {

        validateHealthPlan(healthPlanId);

        HealthPlanCoverageDomain healthPlanCoverage =
                coverageRepository
                        .findByIdAndHealthPlanIdAndDeletedFalse(
                                coverageId,
                                healthPlanId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Coverage not found"
                                )
                        );

        validatePercentage(
                dto.getCoveragePercentage()
        );

        if (dto.getProcedureId() != null &&
                !dto.getProcedureId()
                        .equals(healthPlanCoverage.getProcedureId())) {

            validateProcedure(dto.getProcedureId());

            boolean duplicated =
                    coverageRepository
                            .existsByHealthPlanIdAndProcedureIdAndIdNotAndDeletedFalse(
                                    healthPlanId,
                                    dto.getProcedureId(),
                                    coverageId
                            );

            if (duplicated) {
                throw new RuntimeException(
                        "The Procedure already has a coverage in this health plan"
                );
            }

            healthPlanCoverage.setProcedureId(dto.getProcedureId());
        }

        healthPlanCoverage.setCoveragePercentage(dto.getCoveragePercentage());

        healthPlanCoverage.setCoverageLimit(dto.getCoverageLimit());

        HealthPlanCoverageDomain updated =
                coverageRepository.save(healthPlanCoverage);

        return toDTO(updated);
    }

    public HealthPlanCoverageDTO patch(UUID healthPlanId, UUID coverageId, HealthPlanCoverageDTO dto) {

        validateHealthPlan(healthPlanId);

        HealthPlanCoverageDomain healthPlanCoverage =
                coverageRepository
                        .findByIdAndHealthPlanIdAndDeletedFalse(
                                coverageId,
                                healthPlanId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Coverage not found"
                                )
                        );

        if (dto.getProcedureId() != null &&
                !dto.getProcedureId()
                        .equals(healthPlanCoverage.getProcedureId())) {

            validateProcedure(dto.getProcedureId());

            boolean duplicated =
                    coverageRepository
                            .existsByHealthPlanIdAndProcedureIdAndIdNotAndDeletedFalse(
                                    healthPlanId,
                                    dto.getProcedureId(),
                                    coverageId
                            );

            if (duplicated) {
                throw new RuntimeException(
                        "The procedure already has a coverage in this health plan"
                );
            }

            healthPlanCoverage.setProcedureId(
                    dto.getProcedureId()
            );
        }

        if (dto.getCoveragePercentage() != null) {

            validatePercentage(dto.getCoveragePercentage());

            healthPlanCoverage.setCoveragePercentage(dto.getCoveragePercentage());
        }

        if (dto.getCoverageLimit() != null) {
            healthPlanCoverage.setCoverageLimit(dto.getCoverageLimit());
        }

        HealthPlanCoverageDomain updated =
                coverageRepository.save(healthPlanCoverage);

        return toDTO(updated);
    }

    public void delete(UUID healthPlanId, UUID coverageId) {

        validateHealthPlan(healthPlanId);

        HealthPlanCoverageDomain healthPlanCoverage =
                coverageRepository
                        .findByIdAndHealthPlanIdAndDeletedFalse(
                                coverageId,
                                healthPlanId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Coverage not found"
                                )
                        );

        healthPlanCoverage.setDeleted(true);

        coverageRepository.save(healthPlanCoverage);
    }

    private HealthPlanDomain validateHealthPlan(
            UUID healthPlanId
    ) {

        return healthPlanRepository
                .findByIdAndDeletedFalse(healthPlanId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Health plan not found"
                        )
                );
    }

    private void validatePercentage(
            Integer percentage
    ) {

        if (percentage == null ||
                percentage < 0 ||
                percentage > 100) {

            throw new IllegalArgumentException(
                    "Coverage percentage must be between 0 and 100"
            );
        }
    }

    private void validateProcedure(UUID procedureId) {

        ProcedureDTO[] procedures = restClient
                .get()
                .uri("/api/v1/procedures")
                .header(HttpHeaders.AUTHORIZATION, currentAuthorizationHeader())
                .retrieve()
                .body(ProcedureDTO[].class);

        boolean validProcedure = procedures != null &&
                java.util.Arrays.stream(procedures)
                        .anyMatch(procedure ->
                                procedure.getId().equals(procedureId) &&
                                        Boolean.TRUE.equals(procedure.getActive())
                        );

        if (!validProcedure) {
            throw new RuntimeException(
                    "Procedure not found or inactive"
            );
        }
    }

    private void validateCoverageLimit(UUID healthPlanId) {

        long currentCoverages =
                coverageRepository
                        .findByHealthPlanIdAndDeletedFalse(healthPlanId)
                        .size();

        ProcedureDTO[] procedures = restClient
                .get()
                .uri("/api/v1/procedures")
                .header(HttpHeaders.AUTHORIZATION, currentAuthorizationHeader())
                .retrieve()
                .body(ProcedureDTO[].class);

        long availableProcedures = procedures == null
                ? 0
                : java.util.Arrays.stream(procedures)
                .filter(procedure ->
                        Boolean.TRUE.equals(procedure.getActive()))
                .count();

        if (currentCoverages >= availableProcedures) {
            throw new RuntimeException(
                    "The health plan cannot have more coverages than available procedures"
            );
        }
    }

    private String currentAuthorizationHeader() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }

    private HealthPlanCoverageDTO toDTO(
            HealthPlanCoverageDomain healthPlanCoverage
    ) {

        return new HealthPlanCoverageDTO(
                healthPlanCoverage.getId(),
                healthPlanCoverage.getHealthPlan().getId(),
                healthPlanCoverage.getProcedureId(),
                healthPlanCoverage.getCoveragePercentage(),
                healthPlanCoverage.getCoverageLimit()
        );
    }
}