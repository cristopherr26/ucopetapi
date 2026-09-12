package com.uco.ucopetapi.service.product.impl;

import com.uco.ucopetapi.domain.product.*;
import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.dto.product.AssociatedSupplierDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.repository.product.ProductProviderRepository;
import com.uco.ucopetapi.repository.product.ProductRepository;
import com.uco.ucopetapi.repository.product.StockRepository;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import com.uco.ucopetapi.service.product.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final ProductProviderRepository productProviderRepository;
    private final ProviderJPARepository providerRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              StockRepository stockRepository,
                              ProductProviderRepository productProviderRepository,
                              ProviderJPARepository providerRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.productProviderRepository = productProviderRepository;
        this.providerRepository = providerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> list(ProductType type, ProductStatus status, String category, UUID headquarterId) {
        return productRepository.findByFilter(type, category, status).stream()
                .map(product -> toDto(product, headquarterId))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO getById(UUID id, UUID headquarterId) {
        return toDto(findProductOrThrow(id), headquarterId);
    }

    @Override
    @Transactional
    public ProductDTO create(ProductDTO request) {
        validateForCreate(request);
        ProductDomain product = new ProductDomain(
                UUID.randomUUID(),
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getCategory(),
                request.getSalePrice(),
                ProductStatus.ACTIVE,
                request.getImageUrl()
        );
        product = productRepository.save(product);
        saveProviderAssociations(product, request.getProviders());
        return toDto(product, null);
    }

    @Override
    @Transactional
    public ProductDTO update(UUID id, ProductDTO request) {
        validateForUpdate(request);
        ProductDomain product = findProductOrThrow(id);

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            product.setType(request.getType());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getSalePrice() != null) {
            product.setSalePrice(request.getSalePrice());
        }
        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        product = productRepository.save(product);

        if (request.getProviders() != null) {
            productProviderRepository.deleteByProduct_Id(product.getId());
            saveProviderAssociations(product, request.getProviders());
        }

        return toDto(product, null);
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        ProductDomain product = findProductOrThrow(id);
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }

    private void saveProviderAssociations(ProductDomain product, List<AssociatedSupplierDTO> providers) {
        if (providers == null) {
            return;
        }
        for (AssociatedSupplierDTO supplier : providers) {
            ProviderDomain provider = providerRepository.findById(supplier.getProviderId())
                    .orElseThrow(() -> new NoSuchElementException("Proveedor no encontrado: " + supplier.getProviderId()));
            productProviderRepository.save(new ProductProviderDomain(
                    UUID.randomUUID(), product, provider, supplier.getReferencePrice()
            ));
        }
    }

    private ProductDomain findProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + id));
    }

    private ProductDTO toDto(ProductDomain product, UUID headquarterId) {
        List<AssociatedSupplierDTO> providers = productProviderRepository.findByProduct_Id(product.getId()).stream()
                .map(pp -> new AssociatedSupplierDTO(pp.getProvider().getId(), pp.getReferencePrice()))
                .toList();

        Integer stockAtLocation = null;
        if (headquarterId != null) {
            stockAtLocation = stockRepository.findByProduct_IdAndHeadquarter_Id(product.getId(), headquarterId)
                    .map(StockDomain::getQuantity)
                    .orElse(0);
        }

        return new ProductDTO(
                product.getId(), product.getName(), product.getDescription(), product.getType(),
                product.getCategory(), product.getSalePrice(), product.getStatus(), product.getImageUrl(),
                providers, stockAtLocation
        );
    }

    private void validateForCreate(ProductDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (request.getName().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("El tipo de producto (PRODUCT o SERVICE) es obligatorio");
        }
        if (request.getSalePrice() == null || request.getSalePrice() <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor a cero");
        }
        validateCommonFields(request);
    }

    private void validateForUpdate(ProductDTO request) {
        if (request.getName() != null && request.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre no puede quedar vacío");
        }
        if (request.getName() != null && request.getName().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres");
        }
        if (request.getSalePrice() != null && request.getSalePrice() <= 0) {
            throw new IllegalArgumentException("El precio de venta debe ser mayor a cero");
        }
        validateCommonFields(request);
    }

    private void validateCommonFields(ProductDTO request) {
        if (request.getDescription() != null && request.getDescription().length() > 255) {
            throw new IllegalArgumentException("La descripción no puede superar los 255 caracteres");
        }
        if (request.getCategory() != null && request.getCategory().length() > 50) {
            throw new IllegalArgumentException("La categoría no puede superar los 50 caracteres");
        }
        if (request.getProviders() != null) {
            for (AssociatedSupplierDTO supplier : request.getProviders()) {
                if (supplier.getProviderId() == null) {
                    throw new IllegalArgumentException("Cada proveedor asociado necesita un providerId");
                }
                if (supplier.getReferencePrice() != null && supplier.getReferencePrice() < 0) {
                    throw new IllegalArgumentException("El precio de referencia no puede ser negativo");
                }
            }
        }
    }
}