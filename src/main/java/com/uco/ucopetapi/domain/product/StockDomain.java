package com.uco.ucopetapi.domain.product;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "stocks", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "headquarter_id"}))
public class StockDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private ProductDomain product;

    @Column(name = "headquarter_id", nullable = false, updatable = false)
    private UUID headquarterId;

    @Column(nullable = false)
    private Integer quantity;

    protected StockDomain() {
    }

    public StockDomain(final UUID id, final ProductDomain product, final UUID headquarterId, final Integer quantity) {
        this.id = id;
        this.product = product;
        this.headquarterId = headquarterId;
        this.quantity = quantity;
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

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }
}
