package com.uco.ucopetapi.dto.healthplancoverage;

import java.util.UUID;
import java.math.BigDecimal;

public class HealthPlanCoverageDTO {

    private UUID id;
    private UUID healthPlanId;
    private UUID serviceId;
    private Integer coveragePercentage;
    private BigDecimal coverageLimit;

    public HealthPlanCoverageDTO() {
    }

    public HealthPlanCoverageDTO(
            UUID id,
            UUID healthPlanId,
            UUID serviceId,
            Integer coveragePercentage,
            BigDecimal coverageLimit) {

        this.id = id;
        this.healthPlanId = healthPlanId;
        this.serviceId = serviceId;
        this.coveragePercentage = coveragePercentage;
        this.coverageLimit = coverageLimit;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getHealthPlanId() {
        return healthPlanId;
    }

    public void setHealthPlanId(UUID healthPlanId) {
        this.healthPlanId = healthPlanId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public Integer getCoveragePercentage() {
        return coveragePercentage;
    }

    public void setCoveragePercentage(Integer coveragePercentage) {
        this.coveragePercentage = coveragePercentage;
    }

    public BigDecimal getCoverageLimit() {
        return coverageLimit;
    }

    public void setCoverageLimit(BigDecimal coverageLimit) {
        this.coverageLimit = coverageLimit;
    }
}