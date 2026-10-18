package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "service_headquarters", uniqueConstraints = @UniqueConstraint(columnNames = {"service_id", "headquarter_id"}))
public class CatalogItemHeadquarterDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false, updatable = false)
    private CatalogItemDomain item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "headquarter_id", nullable = false, updatable = false)
    private HeadquarterDomain headquarter;

    protected CatalogItemHeadquarterDomain() {
    }

    public CatalogItemHeadquarterDomain(final UUID id, final CatalogItemDomain item, final HeadquarterDomain headquarter) {
        this.id = id;
        this.item = item;
        this.headquarter = headquarter;
    }

    public UUID getId() {
        return id;
    }

    public CatalogItemDomain getItem() {
        return item;
    }

    public HeadquarterDomain getHeadquarter() {
        return headquarter;
    }
}