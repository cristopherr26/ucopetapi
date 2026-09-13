package com.uco.ucopetapi.service.healthplan;

import com.uco.ucopetapi.domain.healthplan.HealthPlanDomain;
import com.uco.ucopetapi.domain.healthplancoverage.HealthPlanCoverageDomain;
import com.uco.ucopetapi.dto.healthplan.HealthPlanDTO;
import com.uco.ucopetapi.dto.healthplancoverage.HealthPlanCoverageDTO;
import com.uco.ucopetapi.repository.healthplan.IHealthPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class HealthPlanService {

    private final IHealthPlanRepository iHealthPlanRepository;

    public HealthPlanService(IHealthPlanRepository iHealthPlanRepository) {
        this.iHealthPlanRepository = iHealthPlanRepository;
    }

    public List<HealthPlanDTO> findAll() {

        return iHealthPlanRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public HealthPlanDTO findById(UUID id) {

        HealthPlanDomain healthPlanDomain = iHealthPlanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Health plan not found")
                );

        return toDTO(healthPlanDomain);
    }

    public List<HealthPlanDTO> findByInsuranceCompany(
            String insuranceCompany) {

        return iHealthPlanRepository
                .findByInsuranceCompanyIgnoreCase(insuranceCompany)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public HealthPlanDTO save(HealthPlanDTO dto) {

        HealthPlanDomain healthPlan = new HealthPlanDomain();

        healthPlan.setName(dto.getName());
        healthPlan.setInsuranceCompany(dto.getInsuranceCompany());
        healthPlan.setDescription(dto.getDescription());

        HealthPlanDomain savedHealthPlan =
                iHealthPlanRepository.save(healthPlan);

        return toDTO(savedHealthPlan);
    }

    public HealthPlanDTO update(UUID id, HealthPlanDTO dto) {

        HealthPlanDomain healthPlan = iHealthPlanRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Health plan not found")
                );

        healthPlan.setName(dto.getName());
        healthPlan.setInsuranceCompany(dto.getInsuranceCompany());
        healthPlan.setDescription(dto.getDescription());

        HealthPlanDomain updatedHealthPlan =
                iHealthPlanRepository.save(healthPlan);

        return toDTO(updatedHealthPlan);
    }

    public void delete(UUID id) {


        if (!iHealthPlanRepository.existsById(id)) {
            throw new NoSuchElementException(
                    "Health Plan not found with id: " + id
            );
        }

        iHealthPlanRepository.deleteById(id);
    }

    private HealthPlanDTO toDTO(HealthPlanDomain healthPlan) {

        List<HealthPlanCoverageDTO> coverageDTOs =
                healthPlan.getCoverages()
                        .stream()
                        .map(this::toCoverageDTO)
                        .toList();

        return new HealthPlanDTO(
                healthPlan.getId(),
                healthPlan.getName(),
                healthPlan.getInsuranceCompany(),
                healthPlan.getDescription(),
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

