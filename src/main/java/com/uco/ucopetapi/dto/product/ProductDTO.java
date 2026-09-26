package com.uco.ucopetapi.dto.product;

import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class ProductDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final UUID id;
    @NotBlank(groups = CatalogValidationGroups.IOnCreate.class, message = "El nombre del producto es obligatorio")
    @Pattern(groups = CatalogValidationGroups.IOnUpdate.class, regexp = "^(?!\\s*$).+", message = "El nombre no puede quedar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;
    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;
    private String imageUrl;
    private Integer price;
    @NotNull(groups = CatalogValidationGroups.IOnCreate.class, message = "La categoría de IVA es obligatoria")
    private TaxCategory taxCategory;
    @NotNull(groups = CatalogValidationGroups.IOnCreate.class, message = "Debes indicar si el producto se vende al público")
    private Boolean sellable;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean active;
    @NotNull(groups = CatalogValidationGroups.IOnCreate.class, message = "La categoría del producto es obligatoria")
    private ProductCategory category;
    private List<@Valid AssociatedSupplierDTO> providers;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
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