package com.uco.ucopetapi.dto.invoice;

import java.util.UUID;

public class InvoiceItemDTO {

    private UUID id;
    private String description;
    private Integer quantity;
    private Double unitValue;
    private Double subtotal;

    public InvoiceItemDTO() {
    }

    public InvoiceItemDTO(UUID id, String description, Integer quantity, Double unitValue, Double subtotal) {
        this.id = id;
        this.description = description;
        this.quantity = quantity;
        this.unitValue = unitValue;
        this.subtotal = subtotal;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(Double unitValue) {
        this.unitValue = unitValue;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
