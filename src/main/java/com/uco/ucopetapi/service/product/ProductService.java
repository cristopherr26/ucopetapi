package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.ProductDomain;
import com.uco.ucopetapi.domain.product.StockDomain;
import com.uco.ucopetapi.domain.product.enums.ProductCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.messaging.product.ProductDeactivationSenderBroker;
import com.uco.ucopetapi.messaging.product.ProductSaveSenderBroker;
import com.uco.ucopetapi.repository.product.IHeadquarterReferenceRepository;
import com.uco.ucopetapi.repository.product.IProductRepository;
import com.uco.ucopetapi.repository.product.IStockRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Validated
public class ProductService {

    private static final String ITEM = "producto";

    private final IProductRepository productRepository;
    private final IStockRepository stockRepository;
    private final IHeadquarterReferenceRepository headquarterRepository;
    private final ProductSaveSenderBroker productSaveSenderBroker;
    private final ProductDeactivationSenderBroker productDeactivationSenderBroker;

    public ProductService(IProductRepository productRepository,
                          IStockRepository stockRepository,
                          IHeadquarterReferenceRepository headquarterRepository,
                          ProductSaveSenderBroker productSaveSenderBroker,
                          ProductDeactivationSenderBroker productDeactivationSenderBroker) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.headquarterRepository = headquarterRepository;
        this.productSaveSenderBroker = productSaveSenderBroker;
        this.productDeactivationSenderBroker = productDeactivationSenderBroker;
    }

    @Transactional(readOnly = true)
    public List<ProductDTO> list(ProductCategory category, Boolean active, Boolean sellable,
                                 TaxCategory taxCategory, UUID headquarterId) {
        Boolean wantedActive = active != null ? active : Boolean.TRUE;
        Map<UUID, Integer> stockAtLocation = headquarterId == null ? Map.of() : stockAt(headquarterId);
        return productRepository.findAll().stream()
                .filter(product -> category == null || product.getCategory() == category)
                .filter(product -> wantedActive.equals(product.getActive()))
                .filter(product -> sellable == null || sellable.equals(product.getSellable()))
                .filter(product -> taxCategory == null || product.getTaxCategory() == taxCategory)
                .filter(product -> headquarterId == null || stockAtLocation.containsKey(product.getId()))
                .map(product -> toDTO(product, stockAtLocation.get(product.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDTO getById(UUID id, UUID headquarterId) {
        ProductDomain product = findOrThrow(id);
        if (headquarterId == null) {
            return toDTO(product, null);
        }
        requireHeadquarter(headquarterId);
        Integer quantity = stockRepository.findByProduct_IdAndHeadquarterId(id, headquarterId)
                .map(StockDomain::getQuantity)
                .orElse(0);
        return toDTO(product, quantity);
    }

    public ProductDTO create(@Valid ProductDTO product) {
        validateRules(product, null);
        ProductDTO message = toMessage(UUID.randomUUID(), product, true);
        CatalogValidationUtils.requireSaved(
                productSaveSenderBroker.executeAndWait(message, UUID.randomUUID().toString()).orElse(null), ITEM);
        return message;
    }

    public ProductDTO update(UUID id, @Valid ProductDTO product) {
        ProductDomain current = findOrThrow(id);
        validateRules(product, id);
        ProductDTO message = toMessage(id, product, current.getActive());
        CatalogValidationUtils.requireSaved(
                productSaveSenderBroker.executeAndWait(message, UUID.randomUUID().toString()).orElse(null), ITEM);
        return message;
    }

    public void deactivate(UUID id) {
        ProductDomain current = findOrThrow(id);
        CatalogValidationUtils.requireDeactivated(productDeactivationSenderBroker
                .executeAndWait(toDTO(current, null), UUID.randomUUID().toString()).orElse(null), ITEM);
    }

    private void validateRules(ProductDTO product, UUID productId) {
        CatalogValidationUtils.validatePrice(product.getSellable(), product.getPrice(), ITEM);
        boolean nameTaken = productId == null
                ? productRepository.existsByNameIgnoreCase(product.getName())
                : productRepository.existsByNameIgnoreCaseAndIdNot(product.getName(), productId);
        if (nameTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un producto con este nombre");
        }
    }

    private Map<UUID, Integer> stockAt(UUID headquarterId) {
        requireHeadquarter(headquarterId);
        return stockRepository.findByHeadquarterId(headquarterId).stream()
                .collect(Collectors.toMap(stock -> stock.getProduct().getId(), StockDomain::getQuantity));
    }

    private void requireHeadquarter(UUID headquarterId) {
        if (!headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
    }

    private ProductDomain findOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + id));
    }

    private static ProductDTO toMessage(UUID id, ProductDTO product, boolean active) {
        return new ProductDTO(id, product.getName(), product.getDescription(), product.getImageUrl(),
                product.getPrice(), product.getTaxCategory(), Boolean.TRUE.equals(product.getSellable()),
                active, product.getCategory(), List.of(), null);
    }

    private static ProductDTO toDTO(ProductDomain product, Integer stockAtLocation) {
        return new ProductDTO(product.getId(), product.getName(), product.getDescription(), product.getImageUrl(),
                product.getPrice(), product.getTaxCategory(), product.getSellable(), product.getActive(),
                product.getCategory(), List.of(), stockAtLocation);
    }
}
