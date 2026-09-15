package com.uco.ucopetapi.service.product.impl;

import com.uco.ucopetapi.domain.product.*;
import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.dto.product.AssociatedSupplierDTO;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.repository.product.ServiceHeadquarterRepository;
import com.uco.ucopetapi.repository.product.ServiceProviderRepository;
import com.uco.ucopetapi.repository.product.ServiceRepository;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import com.uco.ucopetapi.service.product.ServiceService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Service
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceHeadquarterRepository serviceHeadquarterRepository;
    private final ProviderJPARepository providerRepository;
    private final EntityManager entityManager;

    public ServiceServiceImpl(ServiceRepository serviceRepository,
                              ServiceProviderRepository serviceProviderRepository,
                              ServiceHeadquarterRepository serviceHeadquarterRepository,
                              ProviderJPARepository providerRepository,
                              EntityManager entityManager) {
        this.serviceRepository = serviceRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceHeadquarterRepository = serviceHeadquarterRepository;
        this.providerRepository = providerRepository;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDTO> list(ServiceCategory category, Boolean active, Boolean purchasable, Boolean sellable,
                                 TaxCategory taxCategory, UUID headquarterId) {
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
                request.getPrice(),
                request.getTaxCategory(),
                request.getSellable(),
                true,
                request.getCategory(),
                request.getPurchasable()
        );
        service = serviceRepository.save(service);
        saveProviderAssociations(service, request.getProviders());
        saveHeadquarterAssociations(service, request.getHeadquarterIds());
        return toDto(service);
    }

    @Override
    @Transactional
    public ServiceDTO update(UUID id, ServiceDTO request) {
        validateForUpdate(request);
        ServiceDomain service = findServiceOrThrow(id);

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
        service = serviceRepository.save(service);

        if (request.getProviders() != null) {
            serviceProviderRepository.deleteByService_Id(service.getId());
            saveProviderAssociations(service, request.getProviders());
        }
        if (request.getHeadquarterIds() != null) {
            serviceHeadquarterRepository.deleteByService_Id(service.getId());
            saveHeadquarterAssociations(service, request.getHeadquarterIds());
        }

        return toDto(service);
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
        validateCommonFields(request);
    }

    private void validateForUpdate(ServiceDTO request) {
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

    private void validateCommonFields(ServiceDTO request) {
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
        if (request.getHeadquarterIds() != null) {
            Set<UUID> vistasSedes = new HashSet<>();
            for (UUID hqId : request.getHeadquarterIds()) {
                if (hqId == null) {
                    throw new IllegalArgumentException("La lista de sedes no puede contener valores nulos");
                }
                if (!vistasSedes.add(hqId)) {
                    throw new IllegalArgumentException("No puedes repetir la misma sede más de una vez");
                }
            }
        }
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
            HeadquarterDomain headquarter = entityManager.getReference(HeadquarterDomain.class, headquarterId);
            serviceHeadquarterRepository.save(new ServiceHeadquarterDomain(UUID.randomUUID(), service, headquarter));
        }
    }

    private ServiceDomain findServiceOrThrow(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Servicio no encontrado: " + id));
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