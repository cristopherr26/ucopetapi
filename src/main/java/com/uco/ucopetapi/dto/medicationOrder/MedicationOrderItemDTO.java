package com.uco.ucopetapi.dto.medicationOrder;

import java.util.UUID;

public class MedicationOrderItemDTO {

    private UUID id;
    private Integer lineNumber;
    private UUID productId;
    private String productName;
    private Integer quantity;
    private String dose;
    private String frequency;
    private Integer durationDays;
    private String route;
    private String instructions;

    public MedicationOrderItemDTO() {
    }

    public MedicationOrderItemDTO(final UUID id, final Integer lineNumber, final UUID productId,
                                  final String productName, final Integer quantity, final String dose,
                                  final String frequency, final Integer durationDays, final String route,
                                  final String instructions) {
        this.id = id;
        this.lineNumber = lineNumber;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.dose = dose;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.route = route;
        this.instructions = instructions;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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