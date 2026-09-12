package com.uco.ucopetapi.domain.product;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType type;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private Integer salePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    protected ProductDomain() {
    }

    public ProductDomain(final UUID id, final String name, final String description, final ProductType type,
                         final String category, final Integer salePrice, final ProductStatus status,
                         final String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.category = category;
        this.salePrice = salePrice;
        this.status = status;
        this.imageUrl = imageUrl;
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

    public ProductType getType() {
        return type;
    }

    public void setType(final ProductType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(final Integer salePrice) {
        this.salePrice = salePrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(final ProductStatus status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isPurchasable() {
        return this.type == ProductType.PRODUCT;
    }
}