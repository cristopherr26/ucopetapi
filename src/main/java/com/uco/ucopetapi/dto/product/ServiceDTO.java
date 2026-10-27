package com.uco.ucopetapi.dto.product;

import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class ServiceDTO {

    private final UUID id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private String description;

    @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres")
    private String imageUrl;

    @Positive(message = "Necesita un precio mayor a cero")
    private Integer price;

    @NotNull(message = "La categoría de IVA es obligatoria")
    private TaxCategory taxCategory;

    @NotNull(message = "Es obligatorio indicar si el servicio se vende al público")
    private Boolean sellable;

    private Boolean active;

    @NotNull(message = "La categoría es obligatoria")
    private ServiceCategory category;

    @NotNull(message = "Es obligatorio indicar si el servicio es comprable a un proveedor externo")
    private Boolean purchasable;

    private List<Object> providers;
    private List<UUID> headquarterIds;

    public ServiceDTO(final UUID id, final String name, final String description, final String imageUrl,
                      final Integer price, final TaxCategory taxCategory, final Boolean sellable,
                      final Boolean active, final ServiceCategory category, final Boolean purchasable,
                      final List<Object> providers, final List<UUID> headquarterIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.taxCategory = taxCategory;
        this.sellable = sellable;
        this.active = active;
        this.category = category;
        this.purchasable = purchasable;
        this.providers = providers;
        this.headquarterIds = headquarterIds;
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

    public ServiceCategory getCategory() {
        return category;
    }

    public void setCategory(ServiceCategory category) {
        this.category = category;
    }

    public Boolean getPurchasable() {
        return purchasable;
    }

    public void setPurchasable(Boolean purchasable) {
        this.purchasable = purchasable;
    }

    public List<Object> getProviders() {
        return providers;
    }

    public void setProviders(List<Object> providers) {
        this.providers = providers;
    }

    public List<UUID> getHeadquarterIds() {
        return headquarterIds;
    }

    public void setHeadquarterIds(List<UUID> headquarterIds) {
        this.headquarterIds = headquarterIds;
    }
}