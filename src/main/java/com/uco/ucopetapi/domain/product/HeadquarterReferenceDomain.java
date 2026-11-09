package com.uco.ucopetapi.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Immutable
@Table(name = "headquarter")
public class HeadquarterReferenceDomain {

    @Id
    private UUID id;

    @Column(name = "is_active")
    private boolean active;

    protected HeadquarterReferenceDomain() {
    }

    public UUID getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }
}
