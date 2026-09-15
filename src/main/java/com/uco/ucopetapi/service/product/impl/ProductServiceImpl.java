package com.uco.ucopetapi.service.product.impl;

import com.uco.ucopetapi.domain.product.*;
import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.dto.product.AssociatedSupplierDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.repository.product.ProductProviderRepository;
import com.uco.ucopetapi.repository.product.ProductRepository;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.product.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductProviderRepository productProviderRepository;
    private final ProviderJPARepository providerRepository;
    private final StockService stockService;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductProviderRepository productProviderRepository,
                              ProviderJPARepository providerRepository,
                              StockService stockService) {
        this.productRepository = productRepository;
        this.productProviderRepository = productProviderRepository;
        this.providerRepository = providerRepository;
        this.stockService = stockService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> list(ProductCategory category, Boolean active, Boolean sellable, TaxCategory taxCategory, UUID headquarterId) {
        return productRepository.findByFilter(category, active, sellable, taxCategory).stream()
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
                request.getImageUrl(),
                request.getPrice(),
                request.getTaxCategory(),
                request.getSellable(),
                true,
                request.getCategory()
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
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getTaxCategory() != null) {
            product.setTaxCategory(request.getTaxCategory());
        }
        if (request.getSellable() != null) {
            product.setSellable(request.getSellable());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
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
        product.setActive(false);
        productRepository.save(product);
    }

    private void validateForCreate(ProductDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (request.getName().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres");
        }
        if (request.getCategory() == null) {
            throw new IllegalArgumentException("La categoría del producto es obligatoria");
        }
        if (request.getTaxCategory() == null) {
            throw new IllegalArgumentException("La categoría de IVA es obligatoria");
        }
        if (request.getSellable() == null) {
            throw new IllegalArgumentException("Debes indicar si el producto se vende al público");
        }
        if (Boolean.TRUE.equals(request.getSellable()) && (request.getPrice() == null || request.getPrice() <= 0)) {
            throw new IllegalArgumentException("Un producto vendible necesita un precio mayor a cero");
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
        if (Boolean.TRUE.equals(request.getSellable()) && request.getPrice() != null && request.getPrice() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        validateCommonFields(request);
    }

    private void validateCommonFields(ProductDTO request) {
        if (request.getDescription() != null && request.getDescription().length() > 255) {
            throw new IllegalArgumentException("La descripción no puede superar los 255 caracteres");
        }
        if (request.getProviders() != null) {
            Set<UUID> vistos = new HashSet<>();
            for (AssociatedSupplierDTO supplier : request.getProviders()) {
                if (supplier.getProviderId() == null) {
                    throw new IllegalArgumentException("Cada proveedor asociado necesita un providerId");
                }
                if (!vistos.add(supplier.getProviderId())) {
                    throw new IllegalArgumentException("No puedes asociar el mismo proveedor más de una vez en la misma petición");
                }
                if (supplier.getReferencePrice() != null && supplier.getReferencePrice() < 0) {
                    throw new IllegalArgumentException("El precio de referencia no puede ser negativo");
                }
            }
        }
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
            stockAtLocation = stockService.findByProductAndHeadquarter(product.getId(), headquarterId).getQuantity();
        }

        return new ProductDTO(
                product.getId(), product.getName(), product.getDescription(), product.getImageUrl(),
                product.getPrice(), product.getTaxCategory(), product.getSellable(), product.getActive(),
                product.getCategory(), providers, stockAtLocation
        );
    }
}