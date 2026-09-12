// domain/product/ProductProviderDomain.java
package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.provider.ProviderDomain;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "product_providers", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "provider_id"}))
public class ProductProviderDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private ProductDomain product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false, updatable = false)
    private ProviderDomain provider;

    @Column(name = "reference_price")
    private Integer referencePrice;

    protected ProductProviderDomain() {
    }

    public ProductProviderDomain(final UUID id, final ProductDomain product, final ProviderDomain provider,
                                 final Integer referencePrice) {
        this.id = id;
        this.product = product;
        this.provider = provider;
        this.referencePrice = referencePrice;
    }

    public UUID getId() {
        return id;
    }

    public ProductDomain getProduct() {
        return product;
    }

    public ProviderDomain getProvider() {
        return provider;
    }

    public Integer getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(final Integer referencePrice) {
        this.referencePrice = referencePrice;
    }
}