package com.uco.ucopetapi.domain.transfer;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.dto.transfers.TransferStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class TransferDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_headquarter_id", nullable = false)
    private HeadquarterDomain originHeadquarter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_headquarter_id", nullable = false)
    private HeadquarterDomain destinationHeadquarter;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "observations", length = 500)
    private String observations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private PersonDomain createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private PersonDomain updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public TransferDomain() {
    }

    public TransferDomain(UUID id, HeadquarterDomain originHeadquarter, HeadquarterDomain destinationHeadquarter,
                          UUID productId, Integer quantity, TransferStatus status, String observations,
                          PersonDomain createdBy, LocalDateTime createdAt) {
        this.id = id;
        this.originHeadquarter = originHeadquarter;
        this.destinationHeadquarter = destinationHeadquarter;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.observations = observations;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public HeadquarterDomain getOriginHeadquarter() {
        return originHeadquarter;
    }

    public void setOriginHeadquarter(HeadquarterDomain originHeadquarter) {
        this.originHeadquarter = originHeadquarter;
    }

    public HeadquarterDomain getDestinationHeadquarter() {
        return destinationHeadquarter;
    }

    public void setDestinationHeadquarter(HeadquarterDomain destinationHeadquarter) {
        this.destinationHeadquarter = destinationHeadquarter;
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

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public PersonDomain getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(PersonDomain createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PersonDomain getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(PersonDomain updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}