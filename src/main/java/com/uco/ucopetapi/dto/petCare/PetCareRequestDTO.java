package com.uco.ucopetapi.dto.petCare;

import java.time.LocalDateTime;
import java.util.UUID;

public class PetCareRequestDTO {

    private UUID id;
    private UUID petCareId;
    private PetCareRequestType requestType;
    private UUID productId;
    private UUID procedureId;
    private String itemName;
    private Integer quantity;
    private String notes;
    private UUID orderId;
    private UUID requestedByDoctorId;
    private String requestedByName;
    private LocalDateTime requestedAt;
    private PetCareRequestStatus status;
    private String cancellationReason;

    public PetCareRequestDTO() {
    }

    public PetCareRequestDTO(final UUID id, final UUID petCareId, final PetCareRequestType requestType,
                             final UUID productId, final UUID procedureId, final String itemName,
                             final Integer quantity, final String notes, final UUID orderId,
                             final UUID requestedByDoctorId, final String requestedByName,
                             final LocalDateTime requestedAt, final PetCareRequestStatus status,
                             final String cancellationReason) {
        this.id = id;
        this.petCareId = petCareId;
        this.requestType = requestType;
        this.productId = productId;
        this.procedureId = procedureId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.notes = notes;
        this.orderId = orderId;
        this.requestedByDoctorId = requestedByDoctorId;
        this.requestedByName = requestedByName;
        this.requestedAt = requestedAt;
        this.status = status;
        this.cancellationReason = cancellationReason;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPetCareId() {
        return petCareId;
    }

    public void setPetCareId(UUID petCareId) {
        this.petCareId = petCareId;
    }

    public PetCareRequestType getRequestType() {
        return requestType;
    }

    public void setRequestType(PetCareRequestType requestType) {
        this.requestType = requestType;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(UUID procedureId) {
        this.procedureId = procedureId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public UUID getRequestedByDoctorId() {
        return requestedByDoctorId;
    }

    public void setRequestedByDoctorId(UUID requestedByDoctorId) {
        this.requestedByDoctorId = requestedByDoctorId;
    }

    public String getRequestedByName() {
        return requestedByName;
    }

    public void setRequestedByName(String requestedByName) {
        this.requestedByName = requestedByName;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public PetCareRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PetCareRequestStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
