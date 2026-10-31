package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class ProductDomain extends CatalogItemDomain {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductCategory category;

    public ProductDomain() {
        super();
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(final ProductCategory category) {
        this.category = category;
    }
}