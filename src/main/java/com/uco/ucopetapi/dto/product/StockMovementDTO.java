package com.uco.ucopetapi.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uco.ucopetapi.domain.product.StockMovementDomain;
import com.uco.ucopetapi.domain.product.enums.StockMovementReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class StockMovementDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    @NotNull(message = "El producto es obligatorio")
    private UUID productId;

    @NotNull(message = "La sede es obligatoria")
    private UUID headquarterId;

    @NotNull(message = "La cantidad es obligatoria")
    private Integer quantity;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer balanceAfter;

    private StockMovementReason reason;

    @Size(max = 255, message = "La nota no puede superar los 255 caracteres")
    private String note;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID performedBy;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Instant occurredAt;

    public static StockMovementDTO of(StockMovementDomain movement) {
        StockMovementDTO dto = new StockMovementDTO();
        dto.id = movement.getId();
        dto.productId = movement.getProduct().getId();
        dto.headquarterId = movement.getHeadquarterId();
        dto.quantity = movement.getQuantity();
        dto.balanceAfter = movement.getBalanceAfter();
        dto.reason = movement.getReason();
        dto.note = movement.getNote();
        dto.performedBy = movement.getPerformedBy();
        dto.occurredAt = movement.getOccurredAt();
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getHeadquarterId() {
        return headquarterId;
    }

    public void setHeadquarterId(UUID headquarterId) {
        this.headquarterId = headquarterId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public StockMovementReason getReason() {
        return reason;
    }

    public void setReason(StockMovementReason reason) {
        this.reason = reason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public UUID getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(UUID performedBy) {
        this.performedBy = performedBy;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
