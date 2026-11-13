package com.uco.ucopetapi.integration.sale.catalog;

import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.List;
import java.util.UUID;

public class CatalogItemResponse {

    private UUID id;
    private String name;
    private Integer price;
    private TaxCategory taxCategory;
    private Boolean sellable;
    private Boolean active;
    private List<UUID> headquarterIds;

    public CatalogItemResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public TaxCategory getTaxCategory() {
        return taxCategory;
    }

    public void setTaxCategory(TaxCategory taxCategory) {
        this.taxCategory = taxCategory;
    }

    public Boolean getSellable() {
        return sellable;
    }

    public void setSellable(Boolean sellable) {
        this.sellable = sellable;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<UUID> getHeadquarterIds() {
        return headquarterIds;
    }

    public void setHeadquarterIds(List<UUID> headquarterIds) {
        this.headquarterIds = headquarterIds;
    }
}