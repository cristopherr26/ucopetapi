package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.product.enums.StockMovementReason;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements",
        indexes = @Index(name = "ix_stock_movement_product_headquarter",
                columnList = "product_id, headquarter_id, occurred_at"))
public class StockMovementDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private ProductDomain product;

    @Column(name = "headquarter_id", nullable = false, updatable = false)
    private UUID headquarterId;

    @Column(nullable = false, updatable = false)
    private Integer quantity;

    @Column(name = "balance_after", nullable = false, updatable = false)
    private Integer balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private StockMovementReason reason;

    @Column(updatable = false)
    private String note;

    @Column(name = "performed_by", updatable = false)
    private UUID performedBy;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected StockMovementDomain() {
    }

    public UUID getId() {
        return id;
    }

    public ProductDomain getProduct() {
        return product;
    }

    public UUID getHeadquarterId() {
        return headquarterId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public StockMovementReason getReason() {
        return reason;
    }

    public String getNote() {
        return note;
    }

    public UUID getPerformedBy() {
        return performedBy;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
