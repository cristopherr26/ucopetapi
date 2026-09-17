package com.uco.ucopetapi.service.healthplan;

import com.uco.ucopetapi.domain.healthplan.HealthPlanDomain;
import com.uco.ucopetapi.domain.healthplancoverage.HealthPlanCoverageDomain;
import com.uco.ucopetapi.dto.healthplan.HealthPlanDTO;
import com.uco.ucopetapi.dto.healthplancoverage.HealthPlanCoverageDTO;
import com.uco.ucopetapi.exception.healthplan.HealthPlanNotFoundException;
import com.uco.ucopetapi.repository.healthplan.IHealthPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional

public class HealthPlanService {

    private final IHealthPlanRepository healthPlanRepository;

    public HealthPlanService(IHealthPlanRepository healthPlanRepository) {
        this.healthPlanRepository = healthPlanRepository;
    }

    @Transactional(readOnly = true)
    public List<HealthPlanDTO> findAll() {

        return healthPlanRepository.findByDeletedFalse()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public HealthPlanDTO findById(UUID id) {

        HealthPlanDomain healthPlanDomain = healthPlanRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(HealthPlanNotFoundException::new);

        return toDTO(healthPlanDomain);
    }

    @Transactional(readOnly = true)
    public List<HealthPlanDTO> findByName(String name) {

        return healthPlanRepository
                .findByNameContainingIgnoreCaseAndDeletedFalse(name)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HealthPlanDTO> findByInsuranceCompany(
            String insuranceCompany) {

        return healthPlanRepository
                .findByInsuranceCompanyIgnoreCaseAndDeletedFalse(insuranceCompany)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public HealthPlanDTO save(HealthPlanDTO dto) {

        HealthPlanDomain healthPlan = new HealthPlanDomain();

        healthPlan.setName(dto.getName());
        healthPlan.setInsuranceCompany(dto.getInsuranceCompany());
        healthPlan.setDescription(dto.getDescription());

        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            healthPlan.setStatus(
                    HealthPlanDomain.Status.ACTIVA
            );
        } else {
            healthPlan.setStatus(
                    HealthPlanDomain.Status.valueOf(
                            dto.getStatus().toUpperCase()
                    )
            );
        }

        healthPlan.setDeleted(false);
        healthPlan.setCoverages(new ArrayList<>());

        HealthPlanDomain savedHealthPlan =
                healthPlanRepository.save(healthPlan);

        return toDTO(savedHealthPlan);
    }

    public HealthPlanDTO update(UUID id, HealthPlanDTO dto) {

        HealthPlanDomain healthPlan = healthPlanRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(HealthPlanNotFoundException::new);

        healthPlan.setName(dto.getName());
        healthPlan.setDescription(dto.getDescription());

        if (dto.getStatus() != null &&
                !dto.getStatus().isBlank()) {

            healthPlan.setStatus(
                    HealthPlanDomain.Status.valueOf(
                            dto.getStatus().toUpperCase()
                    )
            );
        }

        HealthPlanDomain updatedHealthPlan =
                healthPlanRepository.save(healthPlan);

        return toDTO(updatedHealthPlan);
    }

    public HealthPlanDTO patch( UUID id, HealthPlanDTO dto) {

        HealthPlanDomain healthPlan = healthPlanRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(HealthPlanNotFoundException::new);

        if (dto.getName() != null) {
            healthPlan.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            healthPlan.setDescription(dto.getDescription());
        }

        if (dto.getStatus() != null) {
            healthPlan.setStatus(
                    HealthPlanDomain.Status.valueOf(
                            dto.getStatus().toUpperCase()
                    )
            );
        }

        HealthPlanDomain updatedHealthPlan =
                healthPlanRepository.save(healthPlan);

        return toDTO(updatedHealthPlan);
    }

    public void delete(UUID id) {


        HealthPlanDomain healthPlan = healthPlanRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(HealthPlanNotFoundException::new);

        healthPlan.setDeleted(true);
        healthPlan.setStatus( HealthPlanDomain.Status.INACTIVA );

        healthPlanRepository.save(healthPlan);
    }

    private HealthPlanDTO toDTO(HealthPlanDomain healthPlan) {

        List<HealthPlanCoverageDTO> coverageDTOs =
                healthPlan.getCoverages()
                        .stream()
                        .filter(coverage -> !coverage.isDeleted())
                        .map(this::toCoverageDTO)
                        .toList();

        return new HealthPlanDTO(
                healthPlan.getId(),
                healthPlan.getName(),
                healthPlan.getInsuranceCompany(),
                healthPlan.getDescription(),
                healthPlan.getStatus().name(),
                coverageDTOs
        );
    }

    private HealthPlanCoverageDTO toCoverageDTO(
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

