package com.uco.ucopetapi.dto.product;

import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.List;
import java.util.UUID;

public class ProductDTO {

    private final UUID id;
    private String name;
    private String description;
    private String imageUrl;
    private Integer price;
    private TaxCategory taxCategory;
    private Boolean sellable;
    private Boolean active;
    private ProductCategory category;
    private List<AssociatedSupplierDTO> providers;
    private Integer stockAtLocation;

    public ProductDTO(final UUID id, final String name, final String description, final String imageUrl,
                      final Integer price, final TaxCategory taxCategory, final Boolean sellable,
                      final Boolean active, final ProductCategory category,
                      final List<AssociatedSupplierDTO> providers, final Integer stockAtLocation) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.taxCategory = taxCategory;
        this.sellable = sellable;
        this.active = active;
        this.category = category;
        this.providers = providers;
        this.stockAtLocation = stockAtLocation;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public List<AssociatedSupplierDTO> getProviders() {
        return providers;
    }

    public void setProviders(List<AssociatedSupplierDTO> providers) {
        this.providers = providers;
    }

    public Integer getStockAtLocation() {
        return stockAtLocation;
    }

    public void setStockAtLocation(Integer stockAtLocation) {
        this.stockAtLocation = stockAtLocation;
    }
}