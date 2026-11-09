package com.uco.ucopetapi.integration.sale.healthPlan;

import java.util.UUID;
import com.uco.ucopetapi.integration.sale.healthPlan.HealthPlanResponse;

public interface HealthPlanClient {

    HealthPlanResponse getHealthPlan(UUID healthPlanId);
}
