package com.uco.ucopetapi.dto.medicationOrder;

import java.util.UUID;

public class MedicationOrderItemRequest {

    private UUID productId;
    private Integer quantity;
    private String dose;
    private String frequency;
    private Integer durationDays;
    private String route;
    private String instructions;

    public MedicationOrderItemRequest() {
    }

    public MedicationOrderItemRequest(final UUID productId, final Integer quantity, final String dose,
                                      final String frequency, final Integer durationDays, final String route,
                                      final String instructions) {
        this.productId = productId;
        this.quantity = quantity;
        this.dose = dose;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.route = route;
        this.instructions = instructions;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
