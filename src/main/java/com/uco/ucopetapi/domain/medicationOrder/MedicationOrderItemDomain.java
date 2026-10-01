package com.uco.ucopetapi.domain.medicationOrder;

import com.uco.ucopetapi.domain.clinical.ClinicalBaseDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "medication_order_items")
public class MedicationOrderItemDomain extends ClinicalBaseDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "medication_order_id", nullable = false, updatable = false)
    private MedicationOrderDomain medicationOrder;

    @Column(name = "line_number", nullable = false, updatable = false)
    private Integer lineNumber;

    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @Column(name = "product_name", nullable = false, updatable = false)
    private String productName;

    @Column(name = "quantity", nullable = false, updatable = false)
    private Integer quantity;

    @Column(name = "dose", nullable = false, updatable = false, length = 100)
    private String dose;

    @Column(name = "frequency", nullable = false, updatable = false, length = 100)
    private String frequency;

    @Column(name = "duration_days", updatable = false)
    private Integer durationDays;

    @Column(name = "route", updatable = false, length = 50)
    private String route;

    @Column(name = "instructions", updatable = false, length = 500)
    private String instructions;

    public MedicationOrderItemDomain() {
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MedicationOrderDomain getMedicationOrder() {
        return medicationOrder;
    }

    public void setMedicationOrder(MedicationOrderDomain medicationOrder) {
        this.medicationOrder = medicationOrder;
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

    @Override
    public void validate() {
        requireNotNull(productId, "El producto es obligatorio");
        requireNotBlank(productName, "El nombre del producto es obligatorio");
        requirePositive(quantity, "La cantidad debe ser mayor que cero");
        requireNotBlank(dose, "La dosis es obligatoria");
        requireMaxLength(dose, 100, "La dosis no puede superar 100 caracteres");
        requireNotBlank(frequency, "La frecuencia es obligatoria");
        requireMaxLength(frequency, 100, "La frecuencia no puede superar 100 caracteres");
        requirePositiveIfPresent(durationDays, "La duración debe ser mayor que cero");
        requireMaxLength(route, 50, "La vía de administración no puede superar 50 caracteres");
        requireMaxLength(instructions, 500, "Las instrucciones no pueden superar 500 caracteres");
    }
}