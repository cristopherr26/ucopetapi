package com.uco.ucopetapi.integration.sale.healthPlan;


import java.math.BigDecimal;
import java.util.UUID;

public class HealthPlanCoverageResponse {

    private UUID id;
    private UUID healthPlanId;
    private UUID serviceId;
    private Integer coveragePercentage;
    private BigDecimal coverageLimit;

    public HealthPlanCoverageResponse() {
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
