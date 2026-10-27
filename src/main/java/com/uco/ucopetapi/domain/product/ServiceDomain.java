package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "services")
public class ServiceDomain extends CatalogItemDomain {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceCategory category;

    @Column(nullable = false)
    private Boolean purchasable;

    protected ServiceDomain() {
        super();
    }

    public ServiceDomain(final UUID id, final String name, final String description, final String imageUrl,
                         final Integer price, final TaxCategory taxCategory, final Boolean sellable,
                         final Boolean active, final ServiceCategory category, final Boolean purchasable) {
        super(id, name, description, imageUrl, price, taxCategory, sellable, active);
        this.category = category;
        this.purchasable = purchasable;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(final ServiceCategory category) {
        this.category = category;
    }

    public Boolean getPurchasable() {
        return purchasable;
    }

    public void setPurchasable(final Boolean purchasable) {
        this.purchasable = purchasable;
    }
}