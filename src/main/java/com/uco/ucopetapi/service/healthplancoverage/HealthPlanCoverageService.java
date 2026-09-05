package com.uco.ucopetapi.service.healthplancoverage;

import com.uco.ucopetapi.domain.healthplan.HealthPlanDomain;
import com.uco.ucopetapi.domain.healthplancoverage.HealthPlanCoverageDomain;
import com.uco.ucopetapi.dto.healthplancoverage.HealthPlanCoverageDTO;
import com.uco.ucopetapi.repository.healthplan.IHealthPlanRepository;
import com.uco.ucopetapi.repository.healthplancoverage.IHealthPlanCoverageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.NoSuchElementException;

@Service
public class HealthPlanCoverageService {

    private final IHealthPlanCoverageRepository coverageRepository;
    private final IHealthPlanRepository healthPlanRepository;

    public HealthPlanCoverageService(
            IHealthPlanCoverageRepository coverageRepository,
            IHealthPlanRepository healthPlanRepository) {

        this.coverageRepository = coverageRepository;
        this.healthPlanRepository = healthPlanRepository;
    }

    public List<HealthPlanCoverageDTO> findAll() {

        return coverageRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public HealthPlanCoverageDTO findById(UUID id) {

        HealthPlanCoverageDomain coverage =
                coverageRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Coverage not found"
                                )
                        );

        return toDTO(coverage);
    }

    public List<HealthPlanCoverageDTO> findByHealthPlanId(
            UUID healthPlanId) {

        return coverageRepository
                .findByHealthPlanId(healthPlanId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public HealthPlanCoverageDTO save(
            HealthPlanCoverageDTO dto) {

        if (dto.getCoveragePercentage() < 0 ||
                dto.getCoveragePercentage() > 100) {

            throw new IllegalArgumentException(
                    "Coverage percentage must be between 0 and 100"
            );
        }

        HealthPlanDomain healthPlan =
                healthPlanRepository.findById(dto.getHealthPlanId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Health plan not found"
                                )
                        );

        boolean exists =
                coverageRepository
                        .existsByHealthPlanIdAndProcedureId(
                                dto.getHealthPlanId(),
                                dto.getProcedureId()
                        );

        if (exists) {
            throw new IllegalArgumentException(
                    "This service already exists in this health plan"
            );
        }

        HealthPlanCoverageDomain coverage =
                new HealthPlanCoverageDomain();

        coverage.setHealthPlan(healthPlan);
        coverage.setProcedureId(dto.getProcedureId());
        coverage.setCoveragePercentage(
                dto.getCoveragePercentage()
        );
        coverage.setCoverageLimit(
                dto.getCoverageLimit()
        );

        HealthPlanCoverageDomain saved =
                coverageRepository.save(coverage);

        return toDTO(saved);
    }

    public void delete(UUID id) {

        if (!coverageRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Coverage not found with id: " + id
            );
        }

        coverageRepository.deleteById(id);
    }

    private HealthPlanCoverageDTO toDTO(
            HealthPlanCoverageDomain coverage) {

        return new HealthPlanCoverageDTO(
                coverage.getId(),
                coverage.getHealthPlan().getId(),
                coverage.getProcedureId(),
                coverage.getCoveragePercentage(),
                coverage.getCoverageLimit()
        );
    }
}