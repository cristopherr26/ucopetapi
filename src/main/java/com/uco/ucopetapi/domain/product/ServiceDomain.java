package com.uco.ucopetapi.domain.product;

import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import jakarta.persistence.*;

@Entity
@Table(name = "services")
public class ServiceDomain extends CatalogItemDomain {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceCategory category;

    @Column(nullable = false)
    private Boolean purchasable;

    public ServiceDomain() {
        super();
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