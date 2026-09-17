package com.uco.ucopetapi.domain.healthPlanCoverage;

import com.uco.ucopetapi.domain.healthPlan.HealthPlanDomain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(
        name = "health_plan_coverages",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"health_plan_id", "procedure_id"}
                )
        }
)
public class HealthPlanCoverageDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_plan_id",  nullable = false)
    private HealthPlanDomain healthPlan;

    @Column(name = "procedure_id", nullable = false)
    private UUID procedureId;

    @Column(nullable = false)
    private Integer coveragePercentage;

    @Column(precision = 15, scale = 2)
    private BigDecimal coverageLimit;

    @Column(nullable = false)
    private boolean deleted = false;

    public HealthPlanCoverageDomain() {
    }

    public HealthPlanCoverageDomain(
            UUID id,
            HealthPlanDomain healthPlan,
            UUID procedureId,
            Integer coveragePercentage,
            BigDecimal coverageLimit,
            boolean deleted
    ) {

        this.id = id;
        this.healthPlan = healthPlan;
        this.procedureId = procedureId;
        this.coveragePercentage = coveragePercentage;
        this.coverageLimit = coverageLimit;
        this.deleted = deleted;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public HealthPlanDomain getHealthPlan() {
        return healthPlan;
    }

    public void setHealthPlan(HealthPlanDomain healthPlan) {
        this.healthPlan = healthPlan;
    }

    public UUID getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(UUID serviceId) {
        this.procedureId = serviceId;
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

    public boolean isDeleted() {return deleted;}

    public void setDeleted(boolean deleted) {this.deleted = deleted;}

}
