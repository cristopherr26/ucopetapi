package com.uco.ucopetapi.domain.petCare;

import com.uco.ucopetapi.domain.clinical.ClinicalBaseDomain;
import com.uco.ucopetapi.dto.petCare.PetCareRequestStatus;
import com.uco.ucopetapi.exception.clinical.ClinicalException;
import com.uco.ucopetapi.dto.petCare.PetCareRequestType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "pet_care_requests", indexes = {
        @Index(name = "idx_pet_care_requests_pet_care_id", columnList = "pet_care_id")
})
public class PetCareRequestDomain extends ClinicalBaseDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "pet_care_id", nullable = false, updatable = false)
    private UUID petCareId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, updatable = false, length = 20)
    private PetCareRequestType requestType;

    @Column(name = "product_id", updatable = false)
    private UUID productId;

    @Column(name = "procedure_id", updatable = false)
    private UUID procedureId;

    @Column(name = "item_name", nullable = false, updatable = false)
    private String itemName;

    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;

    @Column(name = "notes", updatable = false, length = 1000)
    private String notes;

    @Column(name = "order_id", updatable = false)
    private UUID orderId;

    @Column(name = "requested_by_doctor_id", nullable = false, updatable = false)
    private UUID requestedByDoctorId;

    @Column(name = "requested_by_name", nullable = false, updatable = false)
    private String requestedByName;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PetCareRequestStatus status;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    public PetCareRequestDomain() {
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now(ZoneId.of("America/Bogota"));
        }
        if (status == null) {
            status = PetCareRequestStatus.REQUESTED;
        }
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

    @Override
    public void validate() {
        requireNotNull(petCareId, "La atención de la solicitud es obligatoria");
        requireNotNull(requestType, "El tipo de solicitud es obligatorio");
        requireNotNull(requestedByDoctorId, "El veterinario que solicita es obligatorio");
        requireNotBlank(requestedByName, "El nombre de quien solicita es obligatorio");
        requireNotBlank(itemName, "El nombre del ítem es obligatorio");
        requireMaxLength(notes, 1000, "Las notas no pueden superar 1000 caracteres");
        if (requestType == PetCareRequestType.MEDICATION) {
            requireNotNull(productId, "El producto es obligatorio para solicitar un medicamento");
            if (quantity == null) {
                requireNotNull(quantity, "La cantidad es obligatoria para solicitar un medicamento");
            }
            if (quantity != null && quantity <= 0) {
                throw ClinicalException.badRequest("La cantidad debe ser mayor que cero");
            }
        } else if (requestType == PetCareRequestType.PROCEDURE) {
            requireNotNull(procedureId, "El procedimiento es obligatorio para una solicitud de procedimiento");
        } else if (requestType != null) {
            throw ClinicalException.badRequest("El tipo de solicitud no es válido");
        }
    }

    public void cancel(final UUID requestingDoctorId, final String reason) {
        requireState(status == PetCareRequestStatus.REQUESTED, "La solicitud ya fue anulada");
        requireAuthor(requestedByDoctorId != null && requestedByDoctorId.equals(requestingDoctorId),
                "Solo el veterinario que hizo la solicitud puede anularla");
        requireNotBlank(reason, "El motivo de anulación es obligatorio");
        requireMaxLength(reason == null ? null : reason.trim(), 500,
                "El motivo de anulación no puede superar 500 caracteres");
        this.status = PetCareRequestStatus.CANCELLED;
        this.cancellationReason = reason.trim();
    }
}

