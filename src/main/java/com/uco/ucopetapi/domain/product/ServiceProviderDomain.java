package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.provider.ProviderDomain;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "service_providers", uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "provider_id"}))
public class ServiceProviderDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false, updatable = false)
    private ServiceDomain service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false, updatable = false)
    private ProviderDomain provider;

    @Column(name = "reference_price")
    private Integer referencePrice;

    protected ServiceProviderDomain() {
    }

    public ServiceProviderDomain(final UUID id, final ServiceDomain service, final ProviderDomain provider,
                                 final Integer referencePrice) {
        this.id = id;
        this.service = service;
        this.provider = provider;
        this.referencePrice = referencePrice;
    }

    public UUID getId() {
        return id;
    }

    public ServiceDomain getService() {
        return service;
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