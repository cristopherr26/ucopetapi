package com.uco.ucopetapi.domain.healthplan;
import com.uco.ucopetapi.domain.healthplancoverage.HealthPlanCoverageDomain;
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

    @Column(nullable = false)
    private String insuranceCompany;

    private String description;

    @OneToMany(
            mappedBy = "healthPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<HealthPlanCoverageDomain> coverages = new ArrayList<>();

    public HealthPlanDomain() {
    }

    public HealthPlanDomain(
            UUID id,
            String name,
            String insuranceCompany,
            String description,
            List<HealthPlanCoverageDomain> coverages) {

        this.id = id;
        this.name = name;
        this.insuranceCompany = insuranceCompany;
        this.description = description;
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

    public List<HealthPlanCoverageDomain> getCoverages() {
        return coverages;
    }

    public void setCoverages(List<HealthPlanCoverageDomain> coverages) {
        this.coverages = coverages;
    }
}
