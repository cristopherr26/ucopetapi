package com.uco.ucopetapi.service.healthPlan;

import com.uco.ucopetapi.domain.healthPlan.HealthPlanDomain;
import com.uco.ucopetapi.domain.healthPlanCoverage.HealthPlanCoverageDomain;
import com.uco.ucopetapi.dto.healthPlan.HealthPlanDTO;
import com.uco.ucopetapi.dto.healthPlanCoverage.HealthPlanCoverageDTO;
import com.uco.ucopetapi.exception.healthPlan.HealthPlanNotFoundException;
import com.uco.ucopetapi.messaging.healthPlan.HealthPlanCreationSenderBroker;
import com.uco.ucopetapi.messaging.healthPlan.HealthPlanDeactivationSenderBroker;
import com.uco.ucopetapi.messaging.healthPlan.HealthPlanUpdateSenderBroker;
import com.uco.ucopetapi.repository.healthPlan.IHealthPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class HealthPlanService {

    private final IHealthPlanRepository healthPlanRepository;
    private final HealthPlanCreationSenderBroker healthPlanCreationSenderBroker;
    private final HealthPlanUpdateSenderBroker healthPlanUpdateSenderBroker;
    private final HealthPlanDeactivationSenderBroker healthPlanDeactivationSenderBroker;

    public HealthPlanService(
            IHealthPlanRepository healthPlanRepository,
            HealthPlanCreationSenderBroker healthPlanCreationSenderBroker,
            HealthPlanUpdateSenderBroker healthPlanUpdateSenderBroker,
            HealthPlanDeactivationSenderBroker healthPlanDeactivationSenderBroker
    ) {
        this.healthPlanRepository = healthPlanRepository;
        this.healthPlanCreationSenderBroker = healthPlanCreationSenderBroker;
        this.healthPlanUpdateSenderBroker = healthPlanUpdateSenderBroker;
        this.healthPlanDeactivationSenderBroker = healthPlanDeactivationSenderBroker;
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
    public List<HealthPlanDTO> findByInsuranceCompany(String insuranceCompany) {
        return healthPlanRepository
                .findByInsuranceCompanyIgnoreCaseAndDeletedFalse(insuranceCompany)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public HealthPlanDTO save(HealthPlanDTO dto) {
        HealthPlanDomain healthPlan = new HealthPlanDomain();

        healthPlan.setId(UUID.randomUUID());
        healthPlan.setName(dto.getName());
        healthPlan.setInsuranceCompany(dto.getInsuranceCompany());
        healthPlan.setDescription(dto.getDescription());

        if (dto.getStatus() == null || dto.getStatus().isBlank()) {
            healthPlan.setStatus(HealthPlanDomain.Status.ACTIVA);
        } else {
            healthPlan.setStatus(HealthPlanDomain.Status.valueOf(dto.getStatus().toUpperCase()));
        }

        healthPlan.setDeleted(false);
        healthPlan.setCoverages(new ArrayList<>());

        // Se envía a RabbitMQ sin llamar a repository.save()
        String idMessage = UUID.randomUUID().toString();
        healthPlanCreationSenderBroker.execute(healthPlan, idMessage);

        return toDTO(healthPlan);
    }

    @Transactional
    public HealthPlanDTO update(UUID id, HealthPlanDTO dto) {
        HealthPlanDomain healthPlan = healthPlanRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(HealthPlanNotFoundException::new);

        healthPlan.setName(dto.getName());
        healthPlan.setDescription(dto.getDescription());

        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            healthPlan.setStatus(HealthPlanDomain.Status.valueOf(dto.getStatus().toUpperCase()));
        }

        // Se envía a RabbitMQ sin llamar a repository.save()
        String idMessage = UUID.randomUUID().toString();
        healthPlanUpdateSenderBroker.execute(healthPlan, idMessage);

        return toDTO(healthPlan);
    }

    @Transactional
    public HealthPlanDTO patch(UUID id, HealthPlanDTO dto) {
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
            healthPlan.setStatus(HealthPlanDomain.Status.valueOf(dto.getStatus().toUpperCase()));
        }

        // Se envía a RabbitMQ sin llamar a repository.save()
        String idMessage = UUID.randomUUID().toString();
        healthPlanUpdateSenderBroker.execute(healthPlan, idMessage);

        return toDTO(healthPlan);
    }

    @Transactional
    public void delete(UUID id) {
        HealthPlanDomain healthPlan = healthPlanRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(HealthPlanNotFoundException::new);

        healthPlan.setDeleted(true);
        healthPlan.setStatus(HealthPlanDomain.Status.INACTIVA);

        // Se envía a RabbitMQ sin llamar a repository.save()
        String idMessage = UUID.randomUUID().toString();
        healthPlanDeactivationSenderBroker.execute(healthPlan, idMessage);
    }

    private HealthPlanDTO toDTO(HealthPlanDomain healthPlan) {
        List<HealthPlanCoverageDTO> coverageDTOs = new ArrayList<>();
        if (healthPlan.getCoverages() != null) {
            coverageDTOs = healthPlan.getCoverages()
                    .stream()
                    .filter(coverage -> !coverage.isDeleted())
                    .map(this::toCoverageDTO)
                    .toList();
        }

        return new HealthPlanDTO(
                healthPlan.getId(),
                healthPlan.getName(),
                healthPlan.getInsuranceCompany(),
                healthPlan.getDescription(),
                healthPlan.getStatus() != null ? healthPlan.getStatus().name() : null,
                coverageDTOs
        );
    }

    private HealthPlanCoverageDTO toCoverageDTO(HealthPlanCoverageDomain coverage) {
        return new HealthPlanCoverageDTO(
                coverage.getId(),
                coverage.getHealthPlan() != null ? coverage.getHealthPlan().getId() : null,
                coverage.getServiceId(),
                coverage.getProcedureId(),
                coverage.getCoveragePercentage(),
                coverage.getCoverageLimit()
        );
    }
}