package com.uco.ucopetapi.dto.healthplancoverage;

import java.util.UUID;
import java.math.BigDecimal;

public class HealthPlanCoverageDTO {

    private UUID id;
    private UUID healthPlanId;
    private UUID procedureId;
    private Integer coveragePercentage;
    private BigDecimal coverageLimit;

    public HealthPlanCoverageDTO() {
    }

    public HealthPlanCoverageDTO(
            UUID id,
            UUID healthPlanId,
            UUID procedureId,
            Integer coveragePercentage,
            BigDecimal coverageLimit) {

        this.id = id;
        this.healthPlanId = healthPlanId;
        this.procedureId = procedureId;
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

    public UUID getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(UUID procedureId) {
        this.procedureId = procedureId;
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