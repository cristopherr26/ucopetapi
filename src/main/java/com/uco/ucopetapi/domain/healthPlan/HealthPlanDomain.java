package com.uco.ucopetapi.domain.healthPlan;

import com.uco.ucopetapi.domain.healthPlanCoverage.HealthPlanCoverageDomain;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "health_plans")

public class HealthPlanDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private String insuranceCompany;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private boolean deleted = false;

    @OneToMany(
            mappedBy = "healthPlan",
            cascade = CascadeType.ALL
    )
    private List<HealthPlanCoverageDomain> coverages = new ArrayList<>();

    public HealthPlanDomain() {
    }

    public HealthPlanDomain(
            UUID id,
            String name,
            String insuranceCompany,
            String description,
            Status status,
            boolean deleted,
            List<HealthPlanCoverageDomain> coverages) {

        this.id = id;
        this.name = name;
        this.insuranceCompany = insuranceCompany;
        this.description = description;
        this.status = status;
        this.deleted = deleted;
        this.coverages = coverages;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInsuranceCompany() {
        return insuranceCompany;
    }

    public void setInsuranceCompany(String insuranceCompany) {
        this.insuranceCompany = insuranceCompany;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {return status;}

    public void setStatus(Status status) {this.status = status;}

    public boolean isDeleted() {return deleted;}

    public void setDeleted(boolean deleted) {this.deleted = deleted;}

    public List<HealthPlanCoverageDomain> getCoverages() {return coverages;}

    public void setCoverages(List<HealthPlanCoverageDomain> coverages) {this.coverages = coverages;}

    public enum Status {
        ACTIVA,
        INACTIVA
    }
}
