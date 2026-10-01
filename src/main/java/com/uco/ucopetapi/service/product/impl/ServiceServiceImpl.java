package com.uco.ucopetapi.service.product.impl;

import com.uco.ucopetapi.domain.product.*;
import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.dto.product.AssociatedSupplierDTO;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.ServiceHeadquarterRepository;
import com.uco.ucopetapi.repository.product.ServiceProviderRepository;
import com.uco.ucopetapi.repository.product.ServiceRepository;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import com.uco.ucopetapi.service.product.CatalogValidationUtils;
import com.uco.ucopetapi.service.product.ServiceService;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceHeadquarterRepository serviceHeadquarterRepository;
    private final ProviderJPARepository providerRepository;
    private final HeadquarterRepository headquarterRepository;

    public ServiceServiceImpl(ServiceRepository serviceRepository,
                              ServiceProviderRepository serviceProviderRepository,
                              ServiceHeadquarterRepository serviceHeadquarterRepository,
                              ProviderJPARepository providerRepository,
                              com.uco.ucopetapi.repository.headquarter.HeadquarterRepository headquarterRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceHeadquarterRepository = serviceHeadquarterRepository;
        this.providerRepository = providerRepository;
        this.headquarterRepository = headquarterRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDTO> list(ServiceCategory category, Boolean active, Boolean purchasable, Boolean sellable,
                                 TaxCategory taxCategory, UUID headquarterId) {
        if (headquarterId != null && !headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
        return serviceRepository.findByFilter(category, active, purchasable, sellable, taxCategory, headquarterId).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ServiceDTO getById(UUID id) {
        return toDto(findServiceOrThrow(id));
    }

    @Override
    @Transactional
    public ServiceDTO create(ServiceDTO request) {
        validateForCreate(request);
        ServiceDomain service = new ServiceDomain(
                UUID.randomUUID(),
                request.getName(),
                request.getDescription(),
                request.getImageUrl(),
                Boolean.TRUE.equals(request.getSellable()) ? request.getPrice() : null,
                request.getTaxCategory(),
                request.getSellable(),
                true,
                request.getCategory(),
                request.getPurchasable()
        );
        try {
            service = serviceRepository.save(service);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ya existe un servicio con este nombre");
        }
        saveProviderAssociations(service, request.getProviders());
        saveHeadquarterAssociations(service, request.getHeadquarterIds());
        return toDto(service);
    }

    @Override
    @Transactional
    public ServiceDTO update(UUID id, ServiceDTO request) {
        validateForUpdate(request);
        if (request.getName() != null && serviceRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Ya existe un servicio con este nombre");
        }
        ServiceDomain service = findServiceOrThrow(id);

        applyUpdatableFields(service, request);
        applyPriceConsistency(service);
        try {
            service = serviceRepository.save(service);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Ya existe un producto con este nombre");
        }

        applyProviderChanges(service, request.getProviders());
        applyHeadquarterChanges(service, request.getHeadquarterIds());

        return toDto(service);
    }

    private void applyUpdatableFields(ServiceDomain service, ServiceDTO request) {
        if (request.getName() != null) {
            service.setName(request.getName());
        }
        if (request.getDescription() != null) {
            service.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            service.setCategory(request.getCategory());
        }
        if (request.getPrice() != null) {
            service.setPrice(request.getPrice());
        }
        if (request.getTaxCategory() != null) {
            service.setTaxCategory(request.getTaxCategory());
        }
        if (request.getPurchasable() != null) {
            service.setPurchasable(request.getPurchasable());
        }
        if (request.getSellable() != null) {
            service.setSellable(request.getSellable());
        }
        if (request.getActive() != null) {
            service.setActive(request.getActive());
        }
        if (request.getImageUrl() != null) {
            service.setImageUrl(request.getImageUrl());
        }
    }

    private void applyPriceConsistency(ServiceDomain service) {
        if (Boolean.TRUE.equals(service.getSellable())) {
            if (service.getPrice() == null || service.getPrice() <= 0) {
                throw new IllegalArgumentException("Un servicio vendible necesita un precio mayor a cero");
            }
        } else {
            service.setPrice(null);
        }
    }

    private void applyProviderChanges(ServiceDomain service, List<AssociatedSupplierDTO> providers) {
        if (Boolean.FALSE.equals(service.getPurchasable())) {
            if (providers != null && !providers.isEmpty()) {
                throw new IllegalArgumentException("Un servicio no comprable (purchasable = false) no puede tener proveedores asociados");
            }
            serviceProviderRepository.deleteByService_Id(service.getId());
        } else if (providers != null) {
            serviceProviderRepository.deleteByService_Id(service.getId());
            saveProviderAssociations(service, providers);
        }
    }

    private void applyHeadquarterChanges(ServiceDomain service, List<UUID> headquarterIds) {
        if (headquarterIds != null) {
            serviceHeadquarterRepository.deleteByService_Id(service.getId());
            saveHeadquarterAssociations(service, headquarterIds);
        }
    }

    @Override
    @Transactional
    public void deactivate(UUID id) {
        ServiceDomain service = findServiceOrThrow(id);
        service.setActive(false);
        serviceRepository.save(service);
    }

    private void validateForCreate(ServiceDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre del servicio es obligatorio");
        }
        if (request.getName().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres");
        }
        if (serviceRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Ya existe un servicio con este nombre");
        }
        if (request.getCategory() == null) {
            throw new IllegalArgumentException("La categoría del servicio es obligatoria");
        }
        if (request.getTaxCategory() == null) {
            throw new IllegalArgumentException("La categoría de IVA es obligatoria");
        }
        if (request.getPurchasable() == null) {
            throw new IllegalArgumentException("Debes indicar si el servicio es comprable a un proveedor externo");
        }
        if (request.getSellable() == null) {
            throw new IllegalArgumentException("Debes indicar si el servicio se vende al público");
        }
        if (Boolean.TRUE.equals(request.getSellable()) && (request.getPrice() == null || request.getPrice() <= 0)) {
            throw new IllegalArgumentException("Un servicio vendible necesita un precio mayor a cero");
        }
        if (Boolean.FALSE.equals(request.getPurchasable()) && request.getProviders() != null && !request.getProviders().isEmpty()) {
            throw new IllegalArgumentException("Un servicio no comprable (purchasable = false) no puede tener proveedores asociados");
        }
        validateCommonFields(request);
    }

    private void validateForUpdate(ServiceDTO request) {
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

    private void validateCommonFields(ServiceDTO request) {
        if (request.getDescription() != null && request.getDescription().length() > 255) {
            throw new IllegalArgumentException("La descripción no puede superar los 255 caracteres");
        }
        CatalogValidationUtils.validateProviders(request.getProviders());
        CatalogValidationUtils.validateNoDuplicateHeadquarters(request.getHeadquarterIds());
    }

    private void saveProviderAssociations(ServiceDomain service, List<AssociatedSupplierDTO> providers) {
        if (providers == null) {
            return;
        }
        for (AssociatedSupplierDTO supplier : providers) {
            ProviderDomain provider = providerRepository.findById(supplier.getProviderId())
                    .orElseThrow(() -> new NoSuchElementException("Proveedor no encontrado: " + supplier.getProviderId()));
            serviceProviderRepository.save(new ServiceProviderDomain(
                    UUID.randomUUID(), service, provider, supplier.getReferencePrice()
            ));
        }
    }

    private void saveHeadquarterAssociations(ServiceDomain service, List<UUID> headquarterIds) {
        if (headquarterIds == null) {
            return;
        }
        for (UUID headquarterId : headquarterIds) {
            com.uco.ucopetapi.domain.headquarter.HeadquarterDomain headquarter = headquarterRepository.findById(headquarterId)
                    .orElseThrow(() -> new NoSuchElementException("Sede no encontrada: " + headquarterId));
            serviceHeadquarterRepository.save(new ServiceHeadquarterDomain(UUID.randomUUID(), service, headquarter));
        }
    }
    private ServiceDomain findServiceOrThrow(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Servicio no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isActive(UUID id) {
        ServiceDomain service = findServiceOrThrow(id);

        return Boolean.TRUE.equals(service.getActive());
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveServices() {
        return serviceRepository.countActiveServices();
    }

    private ServiceDTO toDto(ServiceDomain service) {
        List<AssociatedSupplierDTO> providers = serviceProviderRepository.findByService_Id(service.getId()).stream()
                .map(sp -> new AssociatedSupplierDTO(sp.getProvider().getId(), sp.getReferencePrice()))
                .toList();

        List<UUID> headquarterIds = serviceHeadquarterRepository.findByService_Id(service.getId()).stream()
                .map(sh -> sh.getHeadquarter().getId())
                .toList();

        return new ServiceDTO(
                service.getId(), service.getName(), service.getDescription(), service.getImageUrl(),
                service.getPrice(), service.getTaxCategory(), service.getSellable(), service.getActive(),
                service.getCategory(), service.getPurchasable(), providers, headquarterIds
        );
    }
}