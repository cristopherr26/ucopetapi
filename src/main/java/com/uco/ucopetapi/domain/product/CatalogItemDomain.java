package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.util.UUID;

@MappedSuperclass
public abstract class CatalogItemDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_category", nullable = false, length = 20)
    private TaxCategory taxCategory;

    @Column(nullable = false)
    private Boolean sellable;

    @Column(nullable = false)
    private Boolean active;

    protected CatalogItemDomain() {
    }

    protected CatalogItemDomain(final UUID id, final String name, final String description, final String imageUrl,
                                final Integer price, final TaxCategory taxCategory, final Boolean sellable,
                                final Boolean active) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.taxCategory = taxCategory;
        this.sellable = sellable;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(final Integer price) {
        this.price = price;
    }

    public TaxCategory getTaxCategory() {
        return taxCategory;
    }

    public void setTaxCategory(final TaxCategory taxCategory) {
        this.taxCategory = taxCategory;
    }

    public Boolean getSellable() {
        return sellable;
    }

    public void setSellable(final Boolean sellable) {
        this.sellable = sellable;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }
}