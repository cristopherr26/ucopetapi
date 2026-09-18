package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "service_headquarters", uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "headquarter_id"}))
public class ServiceHeadquarterDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false, updatable = false)
    private ServiceDomain service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "headquarter_id", nullable = false, updatable = false)
    private HeadquarterDomain headquarter;

    protected ServiceHeadquarterDomain() {
    }

    public ServiceHeadquarterDomain(final UUID id, final ServiceDomain service, final HeadquarterDomain headquarter) {
        this.id = id;
        this.service = service;
        this.headquarter = headquarter;
    }

    public UUID getId() {
        return id;
    }

    public ServiceDomain getService() {
        return service;
    }

    public HeadquarterDomain getHeadquarter() {
        return headquarter;
    }
}