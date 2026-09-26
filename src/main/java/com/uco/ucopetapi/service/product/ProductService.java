package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.*;
import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.dto.product.AssociatedSupplierDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.repository.product.IProductProviderRepository;
import com.uco.ucopetapi.repository.product.IProductRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final IProductRepository productRepository;
    private final IProductProviderRepository productProviderRepository;
    private final ProviderJPARepository providerRepository;
    private final IStockRepository stockRepository;
    private final HeadquarterRepository headquarterRepository;

    public ProductService(IProductRepository productRepository,
                              IProductProviderRepository productProviderRepository,
                              ProviderJPARepository providerRepository,
                              IStockRepository stockRepository,
                              HeadquarterRepository headquarterRepository) {
        this.productRepository = productRepository;
        this.productProviderRepository = productProviderRepository;
        this.providerRepository = providerRepository;
        this.stockRepository = stockRepository;
        this.headquarterRepository = headquarterRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> list(ProductCategory category, Boolean active, Boolean sellable, TaxCategory taxCategory, UUID headquarterId) {
        requireExistingHeadquarter(headquarterId);
        List<ProductDomain> products = productRepository.findByFilter(category, active, sellable, taxCategory, headquarterId);
        if (products.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = products.stream().map(ProductDomain::getId).toList();
        Map<UUID, List<AssociatedSupplierDTO>> providersByProduct = productProviderRepository.findByProduct_IdIn(ids).stream()
                .collect(Collectors.groupingBy(association -> association.getProduct().getId(),
                        Collectors.mapping(ProductService::toSupplierDto, Collectors.toList())));
        Map<UUID, Integer> stockByProduct = headquarterId == null
                ? Map.of()
                : stockRepository.findByProduct_IdInAndHeadquarter_Id(ids, headquarterId).stream()
                        .collect(Collectors.toMap(stock -> stock.getProduct().getId(), StockDomain::getQuantity));
        return products.stream()
                .map(product -> toDto(product,
                        providersByProduct.getOrDefault(product.getId(), List.of()),
                        headquarterId == null ? null : stockByProduct.getOrDefault(product.getId(), 0)))
                .toList();
    }

    private void requireExistingHeadquarter(UUID headquarterId) {
        if (headquarterId != null && !headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
    }

    @Transactional(readOnly = true)
    public ProductDTO getById(UUID id, UUID headquarterId) {
        ProductDomain product = findProductOrThrow(id);
        requireExistingHeadquarter(headquarterId);
        return toDto(product, headquarterId);
    }

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
        product = saveOrConflict(product);
        saveProviderAssociations(product, request.getProviders());
        return toDto(product, null);
    }

    @Transactional
    public ProductDTO update(UUID id, ProductDTO request) {
        validateForUpdate(request);
        ProductDomain product = findProductOrThrow(id);
        CatalogValidationUtils.rejectPriceWhenNotSellable(
                request.getSellable() != null ? request.getSellable() : product.getSellable(),
                request.getPrice(), CatalogValidationUtils.PRODUCT);
        if (request.getName() != null && productRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateCatalogNameException(CatalogValidationUtils.PRODUCT);
        }

        applyUpdatableFields(product, request);
        CatalogItems.applyPriceConsistency(product, CatalogValidationUtils.PRODUCT);
        product = saveOrConflict(product);

        applyProviderChanges(product, request.getProviders());

        return toDto(product, null);
    }

    private void applyUpdatableFields(ProductDomain product, ProductDTO request) {
        CatalogItems.applyCommonFields(product, request.getName(), request.getDescription(),
                request.getImageUrl(), request.getPrice(), request.getTaxCategory(), request.getSellable());
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
    }

    private void applyProviderChanges(ProductDomain product, List<AssociatedSupplierDTO> providers) {
        if (providers != null) {
            productProviderRepository.deleteByProduct_Id(product.getId());
            saveProviderAssociations(product, providers);
        }
    }

    @Transactional
    public void deactivate(UUID id) {
        ProductDomain product = findProductOrThrow(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private void validateForCreate(ProductDTO request) {
        CatalogValidationUtils.requirePriceWhenSellable(request.getSellable(), request.getPrice(),
                CatalogValidationUtils.PRODUCT);
        CatalogValidationUtils.rejectPriceWhenNotSellable(request.getSellable(), request.getPrice(),
                CatalogValidationUtils.PRODUCT);
        if (productRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateCatalogNameException(CatalogValidationUtils.PRODUCT);
        }
        CatalogValidationUtils.validateProviders(request.getProviders());
    }

    private void validateForUpdate(ProductDTO request) {
        CatalogValidationUtils.validatePriceOnUpdate(request.getPrice());
        CatalogValidationUtils.validateProviders(request.getProviders());
    }

    private ProductDomain saveOrConflict(ProductDomain product) {
        try {
            return productRepository.saveAndFlush(product);
        } catch (DataIntegrityViolationException nameTakenBetweenCheckAndInsert) {
            throw new DuplicateCatalogNameException(CatalogValidationUtils.PRODUCT);
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
                .map(ProductService::toSupplierDto)
                .toList();
        Integer stockAtLocation = headquarterId == null
                ? null
                : stockRepository.findByProduct_IdAndHeadquarter_Id(product.getId(), headquarterId)
                        .map(StockDomain::getQuantity)
                        .orElse(0);
        return toDto(product, providers, stockAtLocation);
    }

    private static AssociatedSupplierDTO toSupplierDto(ProductProviderDomain association) {
        return new AssociatedSupplierDTO(association.getProvider().getId(), association.getReferencePrice());
    }

    private static ProductDTO toDto(ProductDomain product, List<AssociatedSupplierDTO> providers,
                                    Integer stockAtLocation) {
        return new ProductDTO(
                product.getId(), product.getName(), product.getDescription(), product.getImageUrl(),
                product.getPrice(), product.getTaxCategory(), product.getSellable(), product.getActive(),
                product.getCategory(), providers, stockAtLocation
        );
    }
}