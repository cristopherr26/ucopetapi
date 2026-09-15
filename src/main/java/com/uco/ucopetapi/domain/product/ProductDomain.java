package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "products")
public class ProductDomain extends CatalogItemDomain {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductCategory category;

    protected ProductDomain() {
        super();
    }

    public ProductDomain(final UUID id, final String name, final String description, final String imageUrl,
                         final Integer price, final TaxCategory taxCategory, final Boolean sellable,
                         final Boolean active, final ProductCategory category) {
        super(id, name, description, imageUrl, price, taxCategory, sellable, active);
        this.category = category;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(final ProductCategory category) {
        this.category = category;
    }
}