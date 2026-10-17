package com.uco.ucopetapi.domain.medicationOrder;

import com.uco.ucopetapi.domain.clinical.ClinicalBaseDomain;
import com.uco.ucopetapi.dto.medicationOrder.MedicationOrderStatus;
import com.uco.ucopetapi.exception.clinical.ClinicalException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "medication_orders", indexes = {
        @Index(name = "idx_medication_orders_pet_care_id", columnList = "pet_care_id"),
        @Index(name = "idx_medication_orders_pet_id", columnList = "pet_id")
})
public class MedicationOrderDomain extends ClinicalBaseDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, updatable = false, length = 32)
    private String orderNumber;

    @Column(name = "pet_care_id", nullable = false, updatable = false)
    private UUID petCareId;

    @Column(name = "episode_id", nullable = false, updatable = false)
    private UUID episodeId;

    @Column(name = "pet_id", nullable = false, updatable = false)
    private UUID petId;

    @Column(name = "notes", updatable = false, length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MedicationOrderStatus status;

    @Column(name = "signed_by_doctor_id", nullable = false, updatable = false)
    private UUID signedByDoctorId;

    @Column(name = "signed_by_person_id", nullable = false, updatable = false)
    private UUID signedByPersonId;

    @Column(name = "signed_by_name", nullable = false, updatable = false)
    private String signedByName;

    @Column(name = "signed_by_license", nullable = false, updatable = false)
    private String signedByLicense;

    @Column(name = "signed_at", nullable = false, updatable = false)
    private LocalDateTime signedAt;

    @Column(name = "signature_hash", nullable = false, updatable = false, length = 64)
    private String signatureHash;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @OneToMany(mappedBy = "medicationOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lineNumber ASC")
    private List<MedicationOrderItemDomain> items = new ArrayList<>();

    public MedicationOrderDomain() {
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (signedAt == null) {
            signedAt = LocalDateTime.now(ZoneId.of("America/Bogota")).truncatedTo(ChronoUnit.SECONDS);
        }
        if (status == null) {
            status = MedicationOrderStatus.ISSUED;
        }
    }

    public void addItem(final MedicationOrderItemDomain item) {
        item.setMedicationOrder(this);
        item.setLineNumber(items.size() + 1);
        items.add(item);
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

    public UUID getSignedByDoctorId() {
        return signedByDoctorId;
    }

    public void setSignedByDoctorId(UUID signedByDoctorId) {
        this.signedByDoctorId = signedByDoctorId;
    }

    public UUID getSignedByPersonId() {
        return signedByPersonId;
    }

    public void setSignedByPersonId(UUID signedByPersonId) {
        this.signedByPersonId = signedByPersonId;
    }

    public String getSignedByName() {
        return signedByName;
    }

    public void setSignedByName(String signedByName) {
        this.signedByName = signedByName;
    }

    public String getSignedByLicense() {
        return signedByLicense;
    }

    public void setSignedByLicense(String signedByLicense) {
        this.signedByLicense = signedByLicense;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
    }

    public String getSignatureHash() {
        return signatureHash;
    }

    public void setSignatureHash(String signatureHash) {
        this.signatureHash = signatureHash;
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

    public List<MedicationOrderItemDomain> getItems() {
        return items;
    }

    public void setItems(List<MedicationOrderItemDomain> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    @Override
    public void validate() {
        requireNotNull(petCareId, "La atención de la orden es obligatoria");
        requireNotNull(episodeId, "El episodio de la orden es obligatorio");
        requireNotNull(petId, "La mascota de la orden es obligatoria");
        requireNotBlank(orderNumber, "El número de la orden es obligatorio");
        requireMaxLength(notes, 1000, "Las notas no pueden superar 1000 caracteres");
        requireNotEmpty(items, "La orden debe tener al menos un medicamento");
        requireMaxSize(items, 30, "La orden no puede incluir más de 30 medicamentos");
        requireNotNull(signedByDoctorId, "El veterinario que firma la orden es obligatorio");
        requireNotNull(signedByPersonId, "La persona que firma la orden es obligatoria");
        requireNotBlank(signedByName, "El nombre de quien firma la orden es obligatorio");
        requireNotBlank(signedByLicense, "La licencia de quien firma la orden es obligatoria");
        requireNotNull(signedAt, "La fecha de la firma es obligatoria");
        requireNotBlank(signatureHash, "La firma de la orden es obligatoria");
        rejectDuplicateProducts();
        for (MedicationOrderItemDomain item : items) {
            item.validate();
        }
    }

    public void cancel(final UUID requestingDoctorId, final String reason, final LocalDateTime when) {
        requireState(status == MedicationOrderStatus.ISSUED,
                "Solo se puede anular una orden de medicamentos en estado emitido");
        requireAuthor(signedByDoctorId != null && signedByDoctorId.equals(requestingDoctorId),
                "Solo el veterinario que firmó la orden puede anularla");
        requireNotBlank(reason, "El motivo es obligatorio");
        requireMaxLength(reason == null ? null : reason.trim(), 500,
                "El motivo de anulación no puede superar 500 caracteres");
        this.status = MedicationOrderStatus.CANCELLED;
        this.cancelledAt = when;
        this.cancellationReason = reason.trim();
    }

    private void rejectDuplicateProducts() {
        Set<UUID> productIds = new HashSet<>();
        for (MedicationOrderItemDomain item : items) {
            if (item.getProductId() != null && !productIds.add(item.getProductId())) {
                throw ClinicalException.badRequest("No se puede repetir un producto en la misma orden");
            }
        }
    }
}

