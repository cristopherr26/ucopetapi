package com.uco.ucopetapi.dto.healthplan;

import com.uco.ucopetapi.dto.healthplancoverage.HealthPlanCoverageDTO;

import java.util.UUID;
import java.util.List;

public class HealthPlanDTO {

    private UUID id;
    private String name;
    private String insuranceCompany;
    private String description;
    private List<HealthPlanCoverageDTO> coverages;

    public HealthPlanDTO() {
    }

    public HealthPlanDTO(
            UUID id,
            String name,
            String insuranceCompany,
            String description,
            List<HealthPlanCoverageDTO> coverages) {

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

    public List<HealthPlanCoverageDTO> getCoverages() {
        return coverages;
    }

    public void setCoverages(List<HealthPlanCoverageDTO> coverages) {
        this.coverages = coverages;
    }
}

