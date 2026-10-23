package com.uco.ucopetapi.integration.sale.healthPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HealthPlanResponse {

    private UUID id;
    private String name;
    private String insuranceCompany;
    private String description;
    private String status;
    private List<HealthPlanCoverageResponse> coverages = new ArrayList<>();

    public HealthPlanResponse() {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<HealthPlanCoverageResponse> getCoverages() {
        return coverages;
    }

    public void setCoverages(List<HealthPlanCoverageResponse> coverages) {
        this.coverages = coverages;
    }
}
