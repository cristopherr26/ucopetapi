package com.uco.ucopetapi.dto.medicationOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MedicationOrderDTO {

    private UUID id;
    private String orderNumber;
    private UUID petCareId;
    private UUID episodeId;
    private UUID petId;
    private String notes;
    private MedicationOrderStatus status;
    private MedicationOrderSignatureDTO signature;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private List<MedicationOrderItemDTO> items = new ArrayList<>();

    public MedicationOrderDTO() {
    }

    public MedicationOrderDTO(final UUID id, final String orderNumber, final UUID petCareId, final UUID episodeId,
                              final UUID petId, final String notes, final MedicationOrderStatus status,
                              final MedicationOrderSignatureDTO signature, final LocalDateTime cancelledAt,
                              final String cancellationReason, final List<MedicationOrderItemDTO> items) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.petCareId = petCareId;
        this.episodeId = episodeId;
        this.petId = petId;
        this.notes = notes;
        this.status = status;
        this.signature = signature;
        this.cancelledAt = cancelledAt;
        this.cancellationReason = cancellationReason;
        this.items = items != null ? items : new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public UUID getPetCareId() {
        return petCareId;
    }

    public void setPetCareId(UUID petCareId) {
        this.petCareId = petCareId;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(UUID episodeId) {
        this.episodeId = episodeId;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public MedicationOrderStatus getStatus() {
        return status;
    }

    public void setStatus(MedicationOrderStatus status) {
        this.status = status;
    }

    public MedicationOrderSignatureDTO getSignature() {
        return signature;
    }

    public void setSignature(MedicationOrderSignatureDTO signature) {
        this.signature = signature;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public List<MedicationOrderItemDTO> getItems() {
        return items;
    }

    public void setItems(List<MedicationOrderItemDTO> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}
