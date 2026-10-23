package com.uco.ucopetapi.integration.sale.healthPlan;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class HealthPlanClientRest implements HealthPlanClient {

    private final RestClient restClient = RestClient.create();

    @Value("${healthplans.service.url:http://localhost:8080/api/v1/healthplans}")
    private String healthPlansServiceUrl;

    @Override
    public HealthPlanResponse getHealthPlan(UUID healthPlanId) {
        return restClient.get()
                .uri(healthPlansServiceUrl + "/{id}", healthPlanId)
                .retrieve()
                .body(HealthPlanResponse.class);
    }
}
