package com.uco.ucopetapi.domain.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "procedures")
public class ProcedureDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 50, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<ProcedureProductDomain> defaultProducts = new ArrayList<>();

    @Column(nullable = false)
    private boolean active = true;

    protected ProcedureDomain() {
        // Required by JPA.
    }

    public ProcedureDomain(final String code, final String type, final String description,
                           final Integer durationMinutes,
                           final List<ProcedureProductDomain> defaultProducts,
                           final boolean active) {
        this.code = code;
        this.type = type;
        this.description = description;
        this.durationMinutes = durationMinutes;
        setDefaultProducts(defaultProducts);
        this.active = active;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public void setCode(final String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(final String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(final String description) { this.description = description; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(final Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public List<ProcedureProductDomain> getDefaultProducts() { return new ArrayList<>(defaultProducts); }
    public void setDefaultProducts(final List<ProcedureProductDomain> defaultProducts) {
        this.defaultProducts = defaultProducts == null ? new ArrayList<>() : new ArrayList<>(defaultProducts);
    }
    public boolean isActive() { return active; }
    public void setActive(final boolean active) { this.active = active; }
}
