package com.uco.ucopetapi.domain.product;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "service_headquarters",
        uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "headquarter_id"}))
public class ServiceHeadquarterDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false, updatable = false)
    private ServiceDomain service;

    @Column(name = "headquarter_id", nullable = false, updatable = false)
    private UUID headquarterId;

    protected ServiceHeadquarterDomain() {
    }

    public ServiceHeadquarterDomain(final UUID id, final ServiceDomain service, final UUID headquarterId) {
        this.id = id;
        this.service = service;
        this.headquarterId = headquarterId;
    }

    public UUID getId() {
        return id;
    }

    public ServiceDomain getService() {
        return service;
    }

    public UUID getHeadquarterId() {
        return headquarterId;
    }
}
