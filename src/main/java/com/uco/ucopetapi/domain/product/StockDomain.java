// domain/product/StockDomain.java
package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "headquarter_id", nullable = false, updatable = false)
    private HeadquarterDomain headquarter;

    @Column(nullable = false)
    private Integer quantity;

    protected StockDomain() {
    }

    public StockDomain(final UUID id, final ProductDomain product, final HeadquarterDomain headquarter,
                       final Integer quantity) {
        this.id = id;
        this.product = product;
        this.headquarter = headquarter;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public ProductDomain getProduct() {
        return product;
    }

    public HeadquarterDomain getHeadquarter() {
        return headquarter;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }
}