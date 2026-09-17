package com.uco.ucopetapi.dto.healthPlan;

import com.uco.ucopetapi.dto.healthPlanCoverage.HealthPlanCoverageDTO;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

public class HealthPlanDTO {

    private UUID id;
    private String name;
    private String insuranceCompany;
    private String description;
    private String status;
    private List<HealthPlanCoverageDTO> coverages = new ArrayList<>();

    public HealthPlanDTO() {
    }

    public HealthPlanDTO(
            UUID id,
            String name,
            String insuranceCompany,
            String description,
            String status,
            List<HealthPlanCoverageDTO> coverages) {

        this.id = id;
        this.name = name;
        this.insuranceCompany = insuranceCompany;
        this.description = description;
        this.status = status;
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

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public List<HealthPlanCoverageDTO> getCoverages() {
        return coverages;
    }

    public void setCoverages(List<HealthPlanCoverageDTO> coverages) {
        this.coverages = coverages;
    }
}

