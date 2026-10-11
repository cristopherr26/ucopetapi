package com.uco.ucopetapi.service.product.impl;

import com.uco.ucopetapi.domain.product.*;
import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.dto.product.AssociatedSupplierDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.repository.product.ProductProviderRepository;
import com.uco.ucopetapi.repository.product.ProductRepository;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import com.uco.ucopetapi.service.product.CatalogValidationUtils;
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.product.StockService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductProviderRepository productProviderRepository;
    private final ProviderJPARepository providerRepository;
    private final StockService stockService;
    private final com.uco.ucopetapi.repository.headquarter.HeadquarterRepository headquarterRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ProductProviderRepository productProviderRepository,
                              ProviderJPARepository providerRepository,
                              StockService stockService,
                              com.uco.ucopetapi.repository.headquarter.HeadquarterRepository headquarterRepository) {
        this.productRepository = productRepository;
        this.productProviderRepository = productProviderRepository;
        this.providerRepository = providerRepository;
        this.stockService = stockService;
        this.headquarterRepository = headquarterRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO> list(ProductCategory category, Boolean active, Boolean sellable, TaxCategory taxCategory, UUID headquarterId) {
        if (headquarterId != null && !headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
        return productRepository.findByFilter(category, active, sellable, taxCategory, headquarterId).stream()
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
                Boolean.TRUE.equals(request.getSellable()) ? request.getPrice() : null,
                request.getTaxCategory(),
                request.getSellable(),
                true,
                request.getCategory()
        );

        try {
            product = productRepository.save(product);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ya existe un producto con este nombre");
        }
        saveProviderAssociations(product, request.getProviders());
        return toDto(product, null);
    }

    @Override
    @Transactional
    public ProductDTO update(UUID id, ProductDTO request) {
        validateForUpdate(request);
        if (request.getName() != null && productRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Ya existe un producto con este nombre");
        }
        ProductDomain product = findProductOrThrow(id);

        applyUpdatableFields(product, request);
        applyPriceConsistency(product);
        try {
            product = productRepository.save(product);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ya existe un producto con este nombre");
        }

        applyProviderChanges(product, request.getProviders());

        return toDto(product, null);
    }

    private void applyUpdatableFields(ProductDomain product, ProductDTO request) {
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
    }

    private void applyPriceConsistency(ProductDomain product) {
        if (Boolean.TRUE.equals(product.getSellable())) {
            if (product.getPrice() == null || product.getPrice() <= 0) {
                throw new IllegalArgumentException("Un producto vendible necesita un precio mayor a cero");
            }
        } else {
            product.setPrice(null);
        }
    }

    private void applyProviderChanges(ProductDomain product, List<AssociatedSupplierDTO> providers) {
        if (providers != null) {
            productProviderRepository.deleteByProduct_Id(product.getId());
            saveProviderAssociations(product, providers);
        }
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
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Ya existe un producto con este nombre");
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
        if (request.getPrice() != null && request.getPrice() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        validateCommonFields(request);
    }

    private void validateCommonFields(ProductDTO request) {
        if (request.getDescription() != null && request.getDescription().length() > 255) {
            throw new IllegalArgumentException("La descripción no puede superar los 255 caracteres");
        }
        CatalogValidationUtils.validateProviders(request.getProviders());
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