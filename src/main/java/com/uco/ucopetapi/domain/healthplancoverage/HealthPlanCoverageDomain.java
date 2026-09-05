package com.uco.ucopetapi.domain.healthplancoverage;

import com.uco.ucopetapi.domain.healthplan.HealthPlanDomain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "health_plan_coverages")
public class HealthPlanCoverageDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_plan_id", nullable = false)
    private HealthPlanDomain healthPlan;

    //TO DOOOO: Cambiar para obtener el id de procedureDomain
    @Column(name = "procedure_id", nullable = false)
    private UUID procedureId;

    @Column(nullable = false)
    private Integer coveragePercentage;

    @Column(precision = 15, scale = 2)
    private BigDecimal coverageLimit;

    public HealthPlanCoverageDomain() {
    }

    public HealthPlanCoverageDomain(
            UUID id,
            HealthPlanDomain healthPlan,
            //TO DO
            UUID procedureId,
            Integer coveragePercentage,
            BigDecimal coverageLimit) {


        this.id = id;
        this.healthPlan = healthPlan;
        //TO DO
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

    public HealthPlanDomain getHealthPlan() {
        return healthPlan;
    }

    public void setHealthPlan(HealthPlanDomain healthPlan) {
        this.healthPlan = healthPlan;
    }

    //TO DO
    public UUID getProcedureId() {
        return procedureId;
    }

    //TO DO
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
}
