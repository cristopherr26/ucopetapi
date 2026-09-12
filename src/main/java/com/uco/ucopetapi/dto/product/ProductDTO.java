package com.uco.ucopetapi.dto.product;

import com.uco.ucopetapi.domain.product.ProductStatus;
import com.uco.ucopetapi.domain.product.ProductType;

import java.util.List;
import java.util.UUID;

public class ProductDTO {

    private UUID id;
    private String name;
    private String description;
    private ProductType type;
    private String category;
    private Integer salePrice;
    private ProductStatus status;
    private String imageUrl;
    private List<AssociatedSupplierDTO> providers;
    private Integer stockAtLocation;

    public ProductDTO(final UUID id, final String name, final String description, final ProductType type,
                      final String category, final Integer salePrice, final ProductStatus status,
                      final String imageUrl, final List<AssociatedSupplierDTO> providers,
                      final Integer stockAtLocation) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.category = category;
        this.salePrice = salePrice;
        this.status = status;
        this.imageUrl = imageUrl;
        this.providers = providers;
        this.stockAtLocation = stockAtLocation;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ProductType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<AssociatedSupplierDTO> getProviders() {
        return providers;
    }

    public Integer getStockAtLocation() {
        return stockAtLocation;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setProviders(List<AssociatedSupplierDTO> providers) {
        this.providers = providers;
    }

    public void setStockAtLocation(Integer stockAtLocation) {
        this.stockAtLocation = stockAtLocation;
    }
}