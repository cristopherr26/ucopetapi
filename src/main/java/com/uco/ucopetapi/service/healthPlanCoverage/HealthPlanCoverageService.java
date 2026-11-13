package com.uco.ucopetapi.service.healthPlanCoverage;

import com.uco.ucopetapi.domain.healthPlan.HealthPlanDomain;
import com.uco.ucopetapi.domain.healthPlanCoverage.HealthPlanCoverageDomain;
import com.uco.ucopetapi.dto.healthPlanCoverage.HealthPlanCoverageDTO;
import com.uco.ucopetapi.exception.healthPlan.*;
import com.uco.ucopetapi.messaging.healthPlanCoverage.HealthPlanCoverageCreationSenderBroker;
import com.uco.ucopetapi.messaging.healthPlanCoverage.HealthPlanCoverageDeactivationSenderBroker;
import com.uco.ucopetapi.messaging.healthPlanCoverage.HealthPlanCoverageUpdateSenderBroker;
import com.uco.ucopetapi.repository.healthPlan.IHealthPlanRepository;
import com.uco.ucopetapi.repository.healthPlanCoverage.IHealthPlanCoverageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HealthPlanCoverageService {

    private final IHealthPlanCoverageRepository coverageRepository;
    private final IHealthPlanRepository healthPlanRepository;
    private final HealthPlanCoverageCreationSenderBroker creationSenderBroker;
    private final HealthPlanCoverageUpdateSenderBroker updateSenderBroker;
    private final HealthPlanCoverageDeactivationSenderBroker deactivationSenderBroker;

    public HealthPlanCoverageService(
            IHealthPlanCoverageRepository coverageRepository,
            IHealthPlanRepository healthPlanRepository,
            HealthPlanCoverageCreationSenderBroker creationSenderBroker,
            HealthPlanCoverageUpdateSenderBroker updateSenderBroker,
            HealthPlanCoverageDeactivationSenderBroker deactivationSenderBroker
    ) {
        this.coverageRepository = coverageRepository;
        this.healthPlanRepository = healthPlanRepository;
        this.creationSenderBroker = creationSenderBroker;
        this.updateSenderBroker = updateSenderBroker;
        this.deactivationSenderBroker = deactivationSenderBroker;
    }

    @Transactional(readOnly = true)
    public List<HealthPlanCoverageDTO> findByHealthPlanId(UUID healthPlanId) {
        validateHealthPlan(healthPlanId);

        return coverageRepository
                .findByHealthPlan_IdAndDeletedFalse(healthPlanId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public HealthPlanCoverageDTO findById(UUID healthPlanId, UUID coverageId) {
        validateHealthPlan(healthPlanId);
        HealthPlanCoverageDomain healthPlanCoverage = findCoverageOrThrow(healthPlanId, coverageId);
        return toDTO(healthPlanCoverage);
    }

    @Transactional
    public HealthPlanCoverageDTO save(UUID healthPlanId, HealthPlanCoverageDTO dto) {
        HealthPlanDomain healthPlan = validateHealthPlan(healthPlanId);

        validatePercentage(dto.getCoveragePercentage());
        validateService(dto.getServiceId());

        boolean duplicated = coverageRepository
                .existsByHealthPlan_IdAndServiceIdAndDeletedFalse(healthPlanId, dto.getServiceId());

        if (duplicated) {
            throw new DuplicateCoverageException();
        }

        HealthPlanCoverageDomain healthPlanCoverage = new HealthPlanCoverageDomain();
        healthPlanCoverage.setHealthPlan(healthPlan);
        healthPlanCoverage.setServiceId(dto.getServiceId());
        healthPlanCoverage.setProcedureId(dto.getProcedureId());
        healthPlanCoverage.setCoveragePercentage(dto.getCoveragePercentage());
        healthPlanCoverage.setCoverageLimit(dto.getCoverageLimit());
        healthPlanCoverage.setDeleted(false);

        healthPlanCoverage = coverageRepository.save(healthPlanCoverage);

        try {
            String idMessage = UUID.randomUUID().toString();
            creationSenderBroker.execute(healthPlanCoverage, idMessage);
        } catch (Exception e) {
            System.err.println("Advertencia RabbitMQ: " + e.getMessage());
        }

        return toDTO(healthPlanCoverage);
    }

    @Transactional
    public HealthPlanCoverageDTO update(UUID healthPlanId, UUID coverageId, HealthPlanCoverageDTO dto) {
        validateHealthPlan(healthPlanId);

        HealthPlanCoverageDomain healthPlanCoverage = findCoverageOrThrow(healthPlanId, coverageId);

        validatePercentage(dto.getCoveragePercentage());
        applyServiceChangeIfNeeded(healthPlanId, coverageId, dto, healthPlanCoverage);

        healthPlanCoverage.setCoveragePercentage(dto.getCoveragePercentage());
        healthPlanCoverage.setCoverageLimit(dto.getCoverageLimit());
        healthPlanCoverage.setProcedureId(dto.getProcedureId());

        healthPlanCoverage = coverageRepository.save(healthPlanCoverage);

        try {
            String idMessage = UUID.randomUUID().toString();
            updateSenderBroker.execute(healthPlanCoverage, idMessage);
        } catch (Exception e) {
            System.err.println("Advertencia RabbitMQ: " + e.getMessage());
        }

        return toDTO(healthPlanCoverage);
    }

    @Transactional
    public HealthPlanCoverageDTO patch(UUID healthPlanId, UUID coverageId, HealthPlanCoverageDTO dto) {
        validateHealthPlan(healthPlanId);

        HealthPlanCoverageDomain healthPlanCoverage = findCoverageOrThrow(healthPlanId, coverageId);

        applyServiceChangeIfNeeded(healthPlanId, coverageId, dto, healthPlanCoverage);

        if (dto.getCoveragePercentage() != null) {
            validatePercentage(dto.getCoveragePercentage());
            healthPlanCoverage.setCoveragePercentage(dto.getCoveragePercentage());
        }

        if (dto.getCoverageLimit() != null) {
            healthPlanCoverage.setCoverageLimit(dto.getCoverageLimit());
        }

        if (dto.getProcedureId() != null) {
            healthPlanCoverage.setProcedureId(dto.getProcedureId());
        }

        healthPlanCoverage = coverageRepository.save(healthPlanCoverage);

        try {
            String idMessage = UUID.randomUUID().toString();
            updateSenderBroker.execute(healthPlanCoverage, idMessage);
        } catch (Exception e) {
            System.err.println("Advertencia RabbitMQ: " + e.getMessage());
        }

        return toDTO(healthPlanCoverage);
    }

    @Transactional
    public void delete(UUID healthPlanId, UUID coverageId) {
        validateHealthPlan(healthPlanId);

        HealthPlanCoverageDomain healthPlanCoverage = findCoverageOrThrow(healthPlanId, coverageId);

        // BORRADO LÓGICO
        healthPlanCoverage.setDeleted(true);
        coverageRepository.save(healthPlanCoverage);

        try {
            String idMessage = UUID.randomUUID().toString();
            deactivationSenderBroker.execute(healthPlanCoverage, idMessage);
        } catch (Exception e) {
            System.err.println("Advertencia RabbitMQ: " + e.getMessage());
        }
    }

    private HealthPlanCoverageDomain findCoverageOrThrow(UUID healthPlanId, UUID coverageId) {
        return coverageRepository
                .findByIdAndHealthPlan_IdAndDeletedFalse(coverageId, healthPlanId)
                .orElseThrow(CoverageNotFoundException::new);
    }

    private void applyServiceChangeIfNeeded(
            UUID healthPlanId,
            UUID coverageId,
            HealthPlanCoverageDTO dto,
            HealthPlanCoverageDomain healthPlanCoverage
    ) {
        if (dto.getServiceId() == null || dto.getServiceId().equals(healthPlanCoverage.getServiceId())) {
            return;
        }

        validateService(dto.getServiceId());

        boolean duplicated = coverageRepository
                .existsByHealthPlan_IdAndServiceIdAndIdNotAndDeletedFalse(healthPlanId, dto.getServiceId(), coverageId);

        if (duplicated) {
            throw new DuplicateCoverageException();
        }

        healthPlanCoverage.setServiceId(dto.getServiceId());
    }

    private HealthPlanDomain validateHealthPlan(UUID healthPlanId) {
        return healthPlanRepository
                .findByIdAndDeletedFalse(healthPlanId)
                .orElseThrow(HealthPlanNotFoundException::new);
    }

    private void validatePercentage(Integer percentage) {
        if (percentage == null || percentage < 1 || percentage > 80) {
            throw new IllegalArgumentException("El porcentaje de cobertura debe estar entre 1 y 80.");
        }
    }

    private void validateService(UUID serviceId) {
        if (serviceId == null) {
            throw new ServiceNotFoundException();
        }
    }

    private HealthPlanCoverageDTO toDTO(HealthPlanCoverageDomain healthPlanCoverage) {
        return new HealthPlanCoverageDTO(
                healthPlanCoverage.getId(),
                healthPlanCoverage.getHealthPlan() != null ? healthPlanCoverage.getHealthPlan().getId() : null,
                healthPlanCoverage.getServiceId(),
                healthPlanCoverage.getProcedureId(),
                healthPlanCoverage.getCoveragePercentage(),
                healthPlanCoverage.getCoverageLimit()
        );
    }
}