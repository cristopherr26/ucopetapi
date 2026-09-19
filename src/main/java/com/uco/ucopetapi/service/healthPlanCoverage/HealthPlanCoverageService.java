package com.uco.ucopetapi.service.healthPlanCoverage;

import com.uco.ucopetapi.domain.healthPlan.HealthPlanDomain;
import com.uco.ucopetapi.domain.healthPlanCoverage.HealthPlanCoverageDomain;
import com.uco.ucopetapi.dto.healthPlanCoverage.HealthPlanCoverageDTO;
import com.uco.ucopetapi.repository.healthPlan.IHealthPlanRepository;
import com.uco.ucopetapi.repository.healthPlanCoverage.IHealthPlanCoverageRepository;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.exception.healthPlan.CoverageLimitExceededException;
import com.uco.ucopetapi.exception.healthPlan.CoverageNotFoundException;
import com.uco.ucopetapi.exception.healthPlan.DuplicateCoverageException;
import com.uco.ucopetapi.exception.healthPlan.HealthPlanNotFoundException;
import com.uco.ucopetapi.exception.healthPlan.ServiceNotFoundException;
import com.uco.ucopetapi.exception.healthPlan.ServiceServiceException;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
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
    public List<HealthPlanCoverageDTO> findByHealthPlanId(UUID healthPlanId) {

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

        HealthPlanCoverageDomain healthPlanCoverage = findCoverageOrThrow(healthPlanId, coverageId);

        return toDTO(healthPlanCoverage);
    }

    public HealthPlanCoverageDTO save( UUID healthPlanId, HealthPlanCoverageDTO dto) {

        HealthPlanDomain healthPlan =
                validateHealthPlan(healthPlanId);

        validatePercentage(dto.getCoveragePercentage());
        validateService(dto.getServiceId());
        validateCoverageLimit(healthPlanId);

        boolean duplicated =
                coverageRepository
                        .existsByHealthPlanIdAndServiceIdAndDeletedFalse(
                                healthPlanId,
                                dto.getServiceId()
                        );

        if (duplicated) {
            throw new DuplicateCoverageException();
        }

        HealthPlanCoverageDomain healthPlanCoverage =
                new HealthPlanCoverageDomain();

        healthPlanCoverage.setHealthPlan(healthPlan);
        healthPlanCoverage.setServiceId(dto.getServiceId());
        healthPlanCoverage.setCoveragePercentage(dto.getCoveragePercentage());
        healthPlanCoverage.setCoverageLimit(dto.getCoverageLimit());
        healthPlanCoverage.setDeleted(false);

        HealthPlanCoverageDomain saved = coverageRepository.save(healthPlanCoverage);

        return toDTO(saved);
    }

    public HealthPlanCoverageDTO update(UUID healthPlanId, UUID coverageId, HealthPlanCoverageDTO dto) {

            validateHealthPlan(healthPlanId);

            HealthPlanCoverageDomain healthPlanCoverage = findCoverageOrThrow(healthPlanId, coverageId);

            validatePercentage(dto.getCoveragePercentage());

            applyServiceChangeIfNeeded(healthPlanId, coverageId, dto, healthPlanCoverage);

            healthPlanCoverage.setCoveragePercentage(dto.getCoveragePercentage());
            healthPlanCoverage.setCoverageLimit(dto.getCoverageLimit());

            HealthPlanCoverageDomain updated = coverageRepository.save(healthPlanCoverage);

            return toDTO(updated);
        }


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

        HealthPlanCoverageDomain updated = coverageRepository.save(healthPlanCoverage);

        return toDTO(updated);
    }

    public void delete(UUID healthPlanId, UUID coverageId) {

        validateHealthPlan(healthPlanId);

        HealthPlanCoverageDomain healthPlanCoverage = findCoverageOrThrow(healthPlanId, coverageId);

        healthPlanCoverage.setDeleted(true);

        coverageRepository.save(healthPlanCoverage);
    }

    private HealthPlanCoverageDomain findCoverageOrThrow(UUID healthPlanId, UUID coverageId) {
        return coverageRepository
                .findByIdAndHealthPlanIdAndDeletedFalse(coverageId, healthPlanId)
                .orElseThrow(CoverageNotFoundException::new);
    }

    private void applyServiceChangeIfNeeded(
            UUID healthPlanId,
            UUID coverageId,
            HealthPlanCoverageDTO dto,
            HealthPlanCoverageDomain healthPlanCoverage
    ) {

        if (dto.getServiceId() == null ||
                dto.getServiceId().equals(healthPlanCoverage.getServiceId())) {
            return;
        }

        validateService(dto.getServiceId());

        boolean duplicated = coverageRepository
                .existsByHealthPlanIdAndServiceIdAndIdNotAndDeletedFalse(
                        healthPlanId,
                        dto.getServiceId(),
                        coverageId
                );

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

        if (percentage == null || percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException(
                    "Coverage percentage must be between 0 and 100"
            );
        }
    }

    private void validateService(UUID serviceId) {

        ServiceDTO[] services = fetchActiveServices();

        boolean validService = Arrays.stream(services)
                .anyMatch(service ->
                        service.getId().equals(serviceId) &&
                                Boolean.TRUE.equals(service.getActive())
                );

        if (!validService) {
            throw new ServiceNotFoundException();
        }
    }

    private void validateCoverageLimit(UUID healthPlanId) {

        long currentCoverages = coverageRepository
                .findByHealthPlanIdAndDeletedFalse(healthPlanId)
                .size();

        long availableservices = fetchActiveServices().length;

        if (currentCoverages >= availableservices) {
            throw new CoverageLimitExceededException();
        }
    }

    private ServiceDTO[] fetchActiveServices() {

        try {
            ServiceDTO[] services = restClient
                    .get()
                    .uri("/api/v1/services")
                    .header(HttpHeaders.AUTHORIZATION, currentAuthorizationHeader())
                    .retrieve()
                    .body(ServiceDTO[].class);

            return services == null
                    ? new ServiceDTO[0]
                    : Arrays.stream(services)
                    .filter(service -> Boolean.TRUE.equals(service.getActive()))
                    .toArray(ServiceDTO[]::new);

        } catch (RestClientException ex) {
            throw new ServiceServiceException(
                    "Could not validate service: the services service is unavailable",
                    ex
            );
        }
    }

    private String currentAuthorizationHeader() {

        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null) {
            throw new ServiceServiceException(
                    "No active HTTP request found to propagate the authorization header"
            );
        }

        return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }

    private HealthPlanCoverageDTO toDTO(HealthPlanCoverageDomain healthPlanCoverage) {

        return new HealthPlanCoverageDTO(
                healthPlanCoverage.getId(),
                healthPlanCoverage.getHealthPlan().getId(),
                healthPlanCoverage.getServiceId(),
                healthPlanCoverage.getCoveragePercentage(),
                healthPlanCoverage.getCoverageLimit()
        );
    }
}