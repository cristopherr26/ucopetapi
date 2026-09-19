package com.uco.ucopetapi.service.sale.impl;

import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.domain.sale.ItemSaleDomain;
import com.uco.ucopetapi.domain.sale.SaleOrderDomain;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.dto.sale.AddItemSaleRequestDTO;
import com.uco.ucopetapi.dto.sale.EditItemSaleRequestDTO;
import com.uco.ucopetapi.dto.sale.ItemSaleDTO;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import com.uco.ucopetapi.dto.sale.enums.ItemType;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;
import com.uco.ucopetapi.repository.sale.ItemSaleRepository;
import com.uco.ucopetapi.repository.sale.SaleOrderRepository;
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.product.ServiceService;
import com.uco.ucopetapi.service.sale.ItemSaleService;
import com.uco.ucopetapi.service.sale.exception.SaleOrderNotFoundException;
import com.uco.ucopetapi.service.sale.exception.SaleOrderStateConflictException;
import com.uco.ucopetapi.service.sale.exception.SaleOrderValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ItemSaleServiceImpl implements ItemSaleService {

    private final ItemSaleRepository itemSaleRepository;
    private final SaleOrderRepository saleOrderRepository;
    private final ProductService productService;
    private final ServiceService serviceService;

    public ItemSaleServiceImpl(ItemSaleRepository itemSaleRepository, SaleOrderRepository saleOrderRepository, ProductService productService, ServiceService serviceService) {
        this.itemSaleRepository = itemSaleRepository;
        this.saleOrderRepository = saleOrderRepository;
        this.productService = productService;
        this.serviceService = serviceService;
    }

    @Override
    @Transactional
    public SaleOrderDTO addItemToSaleOrder(UUID saleOrderId, AddItemSaleRequestDTO request) {
        SaleOrderDomain order = findOrderOrThrow(saleOrderId);
        requireBorrador(order);
        validateRequest(request);
        TaxCategory taxCategory = resolveAndValidateCatalogItem(request.getItemId(), request.getItemType(), order.getHeadquarterId());
        int unitPrice = request.getUnitPrice();
        int quantity = request.getQuantity();
        int subtotal = unitPrice * quantity;

        ItemSaleDomain newItem = new ItemSaleDomain(
                UUID.randomUUID(),
                saleOrderId,
                request.getItemId(),
                request.getItemType(),
                taxCategory,
                quantity,
                unitPrice,
                subtotal
        );
        itemSaleRepository.save(newItem);

        recalculateOrderTotals(order);
        SaleOrderDomain saved = saleOrderRepository.save(order);
        return saved.toDTO();
    }

    @Override
    @Transactional
    public SaleOrderDTO editItemInSaleOrder(UUID saleOrderId, UUID itemSaleId, EditItemSaleRequestDTO request) {
        SaleOrderDomain order = findOrderOrThrow(saleOrderId);
        requireBorrador(order);

        ItemSaleDomain item = itemSaleRepository.findById(itemSaleId)
                .orElseThrow(() -> new SaleOrderValidationException("No existe un item con id " + itemSaleId + " en esta orden."));

        if (!item.getSaleOrderId().equals(saleOrderId)) {
            throw new SaleOrderValidationException("El item " + itemSaleId + " no pertenece a la orden " + saleOrderId + ".");
        }

        validateEditRequest(request);

        int quantity = request.getQuantity();
        int unitPrice = request.getUnitPrice();
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setSubtotal(quantity * unitPrice);
        itemSaleRepository.save(item);

        recalculateOrderTotals(order);
        SaleOrderDomain saved = saleOrderRepository.save(order);
        return saved.toDTO();
    }

    @Override
    @Transactional
    public SaleOrderDTO removeItemFromSaleOrder(UUID saleOrderId, UUID itemSaleId) {
        SaleOrderDomain order = findOrderOrThrow(saleOrderId);
        requireBorrador(order);

        ItemSaleDomain item = itemSaleRepository.findById(itemSaleId)
                .orElseThrow(() -> new SaleOrderValidationException("No existe un item con id " + itemSaleId + " en esta orden."));

        if (!item.getSaleOrderId().equals(saleOrderId)) {
            throw new SaleOrderValidationException("El item " + itemSaleId + " no pertenece a la orden " + saleOrderId + ".");
        }

        itemSaleRepository.deleteById(itemSaleId);

        recalculateOrderTotals(order);
        SaleOrderDomain saved = saleOrderRepository.save(order);
        return saved.toDTO();
    }

    @Override
    public List<ItemSaleDTO> listItemsBySaleOrder(UUID saleOrderId) {
        findOrderOrThrow(saleOrderId);
        return itemSaleRepository.findBySaleOrderId(saleOrderId).stream()
                .map(ItemSaleDomain::toDTO)
                .collect(Collectors.toList());
    }

    private SaleOrderDomain findOrderOrThrow(UUID saleOrderId) {
        return saleOrderRepository.findById(saleOrderId)
                .orElseThrow(() -> new SaleOrderNotFoundException("No existe una orden de venta con id " + saleOrderId));
    }

    private void requireBorrador(SaleOrderDomain order) {
        if (order.getState() != SaleOrderState.BORRADOR) {
            throw new SaleOrderStateConflictException("No se pueden agregar ni quitar items de una orden en estado " + order.getState() + ". Solo se permite mientras está en BORRADOR.");
        }
    }

    private void validateRequest(AddItemSaleRequestDTO request) {
        if (request.getItemId() == null) {
            throw new SaleOrderValidationException("itemId es obligatorio.");
        }
        if (request.getItemType() == null) {
            throw new SaleOrderValidationException("itemType es obligatorio (PRODUCTO o SERVICIO).");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new SaleOrderValidationException("quantity debe ser mayor a 0.");
        }
        if (request.getUnitPrice() == null || request.getUnitPrice() < 0) {
            throw new SaleOrderValidationException("unitPrice no puede ser negativo.");
        }
    }

    private void validateEditRequest(EditItemSaleRequestDTO request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new SaleOrderValidationException("quantity debe ser mayor a 0.");
        }
        if (request.getUnitPrice() == null || request.getUnitPrice() < 0) {
            throw new SaleOrderValidationException("unitPrice no puede ser negativo.");
        }
    }

    private TaxCategory resolveAndValidateCatalogItem(UUID itemId, ItemType itemType, UUID headquarterId) {
        if (itemType == ItemType.PRODUCTO) {
            ProductDTO product;
            try {
                product = productService.getById(itemId, headquarterId);
            } catch (NoSuchElementException ex) {
                throw new SaleOrderValidationException("No existe un producto con id " + itemId);
            }
            if (!Boolean.TRUE.equals(product.getSellable()) || !Boolean.TRUE.equals(product.getActive())) {
                throw new SaleOrderValidationException("El producto " + itemId + " no está disponible para la venta.");
            }
            return product.getTaxCategory();
        }

        ServiceDTO service;
        try {
            service = serviceService.getById(itemId);
        } catch (NoSuchElementException ex) {
            throw new SaleOrderValidationException("No existe un servicio con id " + itemId);
        }
        if (!Boolean.TRUE.equals(service.getSellable()) || !Boolean.TRUE.equals(service.getActive())) {
            throw new SaleOrderValidationException("El servicio " + itemId + " no está disponible para la venta.");
        }
        List<UUID> headquarterIds = service.getHeadquarterIds();
        if (headquarterIds == null || !headquarterIds.contains(headquarterId)) {
            throw new SaleOrderValidationException("El servicio " + itemId + " no está ofrecido en la sede de esta orden.");
        }
        return service.getTaxCategory();
    }

    private void recalculateOrderTotals(SaleOrderDomain order) {
        List<ItemSaleDomain> items = itemSaleRepository.findBySaleOrderId(order.getId());

        int subtotal = 0;
        int totalTaxes = 0;

        for (ItemSaleDomain item : items) {
            subtotal += item.getSubtotal();
            totalTaxes += item.getSubtotal() * item.getTaxCategory().getPercentage() / 100;
        }

        order.setSubtotal(subtotal);
        order.setTotalTaxes(totalTaxes);
        order.setTotal(subtotal - order.getTotalDiscount() + totalTaxes);
    }



}
