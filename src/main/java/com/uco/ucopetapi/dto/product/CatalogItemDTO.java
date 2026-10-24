package com.uco.ucopetapi.dto.product;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uco.ucopetapi.domain.product.enums.CatalogCategory;
import com.uco.ucopetapi.domain.product.enums.CatalogItemType;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.CatalogValidationGroups.IOnCreate;
import com.uco.ucopetapi.dto.product.CatalogValidationGroups.IOnUpdate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = CatalogItemDTO.Builder.class)
public class CatalogItemDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final UUID id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final CatalogItemType type;

    @NotBlank(groups = IOnCreate.class, message = "El nombre es obligatorio")
    @Pattern(groups = IOnUpdate.class, regexp = "^(?!\\s*$).+", message = "El nombre no puede quedar vacío")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar los 255 caracteres")
    private final String description;

    private final String imageUrl;

    @Digits(integer = 13, fraction = 2, message = "El precio admite hasta 13 enteros y 2 decimales")
    private final BigDecimal price;

    @NotNull(groups = IOnCreate.class, message = "La categoría de IVA es obligatoria")
    private final TaxCategory taxCategory;

    @NotNull(groups = IOnCreate.class, message = "Debes indicar si se vende al público")
    private final Boolean sellable;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Boolean active;

    @NotNull(groups = IOnCreate.class, message = "La categoría es obligatoria")
    private final CatalogCategory category;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Boolean purchasable;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final List<UUID> headquarterIds;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final Integer stockAtLocation;

    private CatalogItemDTO(Builder builder) {
        this.id = builder.id;
        this.type = builder.type;
        this.name = builder.name;
        this.description = builder.description;
        this.imageUrl = builder.imageUrl;
        this.price = builder.price;
        this.taxCategory = builder.taxCategory;
        this.sellable = builder.sellable;
        this.active = builder.active;
        this.category = builder.category;
        this.purchasable = builder.purchasable;
        this.headquarterIds = builder.headquarterIds;
        this.stockAtLocation = builder.stockAtLocation;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() {
        return id;
    }

    public CatalogItemType getType() {
        return type;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public TaxCategory getTaxCategory() {
        return taxCategory;
    }

    public Boolean getSellable() {
        return sellable;
    }

    public Boolean getActive() {
        return active;
    }

    public CatalogCategory getCategory() {
        return category;
    }

    public Boolean getPurchasable() {
        return purchasable;
    }

    public List<UUID> getHeadquarterIds() {
        return headquarterIds == null ? null : Collections.unmodifiableList(headquarterIds);
    }

    public Integer getStockAtLocation() {
        return stockAtLocation;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private UUID id;
        private CatalogItemType type;
        private String name;
        private String description;
        private String imageUrl;
        private BigDecimal price;
        private TaxCategory taxCategory;
        private Boolean sellable;
        private Boolean active;
        private CatalogCategory category;
        private Boolean purchasable;
        private List<UUID> headquarterIds;
        private Integer stockAtLocation;

        private Builder() {
        }

        @JsonIgnore
        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        @JsonIgnore
        public Builder type(CatalogItemType type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder taxCategory(TaxCategory taxCategory) {
            this.taxCategory = taxCategory;
            return this;
        }

        public Builder sellable(Boolean sellable) {
            this.sellable = sellable;
            return this;
        }

        @JsonIgnore
        public Builder active(Boolean active) {
            this.active = active;
            return this;
        }

        public Builder category(CatalogCategory category) {
            this.category = category;
            return this;
        }

        public Builder purchasable(Boolean purchasable) {
            this.purchasable = purchasable;
            return this;
        }

        public Builder headquarterIds(List<UUID> headquarterIds) {
            this.headquarterIds = headquarterIds;
            return this;
        }

        @JsonIgnore
        public Builder stockAtLocation(Integer stockAtLocation) {
            this.stockAtLocation = stockAtLocation;
            return this;
        }

        public CatalogItemDTO build() {
            return new CatalogItemDTO(this);
        }
    }
}
