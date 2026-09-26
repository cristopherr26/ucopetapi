package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.*;
import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.dto.product.AssociatedSupplierDTO;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.product.IServiceHeadquarterRepository;
import com.uco.ucopetapi.repository.product.IServiceProviderRepository;
import com.uco.ucopetapi.repository.product.IServiceRepository;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    private final IServiceRepository serviceRepository;
    private final IServiceProviderRepository serviceProviderRepository;
    private final IServiceHeadquarterRepository serviceHeadquarterRepository;
    private final ProviderJPARepository providerRepository;
    private final HeadquarterRepository headquarterRepository;

    public ServiceService(IServiceRepository serviceRepository,
                              IServiceProviderRepository serviceProviderRepository,
                              IServiceHeadquarterRepository serviceHeadquarterRepository,
                              ProviderJPARepository providerRepository,
                              com.uco.ucopetapi.repository.headquarter.HeadquarterRepository headquarterRepository) {
        this.serviceRepository = serviceRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceHeadquarterRepository = serviceHeadquarterRepository;
        this.providerRepository = providerRepository;
        this.headquarterRepository = headquarterRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> list(ServiceCategory category, Boolean active, Boolean purchasable, Boolean sellable,
                                 TaxCategory taxCategory, UUID headquarterId) {
        if (headquarterId != null && !headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
        List<ServiceDomain> services = serviceRepository.findByFilter(category, active, purchasable, sellable, taxCategory, headquarterId);
        if (services.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = services.stream().map(ServiceDomain::getId).toList();
        Map<UUID, List<AssociatedSupplierDTO>> providersByService = serviceProviderRepository.findByService_IdIn(ids).stream()
                .collect(Collectors.groupingBy(association -> association.getService().getId(),
                        Collectors.mapping(ServiceService::toSupplierDto, Collectors.toList())));
        Map<UUID, List<UUID>> headquartersByService = serviceHeadquarterRepository.findByService_IdIn(ids).stream()
                .collect(Collectors.groupingBy(association -> association.getService().getId(),
                        Collectors.mapping(association -> association.getHeadquarter().getId(), Collectors.toList())));
        return services.stream()
                .map(service -> toDto(service,
                        providersByService.getOrDefault(service.getId(), List.of()),
                        headquartersByService.getOrDefault(service.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceDTO getById(UUID id) {
        return toDto(findServiceOrThrow(id));
    }

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
        service = saveOrConflict(service);
        saveProviderAssociations(service, request.getProviders());
        saveHeadquarterAssociations(service, request.getHeadquarterIds());
        return toDto(service);
    }

    @Transactional
    public ServiceDTO update(UUID id, ServiceDTO request) {
        validateForUpdate(request);
        ServiceDomain service = findServiceOrThrow(id);
        CatalogValidationUtils.rejectPriceWhenNotSellable(
                request.getSellable() != null ? request.getSellable() : service.getSellable(),
                request.getPrice(), CatalogValidationUtils.SERVICE);
        if (request.getName() != null && serviceRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new DuplicateCatalogNameException(CatalogValidationUtils.SERVICE);
        }

        applyUpdatableFields(service, request);
        CatalogItems.applyPriceConsistency(service, CatalogValidationUtils.SERVICE);
        service = saveOrConflict(service);

        applyProviderChanges(service, request.getProviders());
        applyHeadquarterChanges(service, request.getHeadquarterIds());

        return toDto(service);
    }

    private void applyUpdatableFields(ServiceDomain service, ServiceDTO request) {
        CatalogItems.applyCommonFields(service, request.getName(), request.getDescription(),
                request.getImageUrl(), request.getPrice(), request.getTaxCategory(), request.getSellable());
        if (request.getCategory() != null) {
            service.setCategory(request.getCategory());
        }
        if (request.getPurchasable() != null) {
            service.setPurchasable(request.getPurchasable());
        }
    }

    private void applyProviderChanges(ServiceDomain service, List<AssociatedSupplierDTO> providers) {
        if (Boolean.FALSE.equals(service.getPurchasable())) {
            requireNoProvidersWhenNotPurchasable(false, providers);
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

    @Transactional
    public void deactivate(UUID id) {
        ServiceDomain service = findServiceOrThrow(id);
        service.setActive(false);
        serviceRepository.save(service);
    }

    private void validateForCreate(ServiceDTO request) {
        if (serviceRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateCatalogNameException(CatalogValidationUtils.SERVICE);
        }
        CatalogValidationUtils.requirePriceWhenSellable(request.getSellable(), request.getPrice(),
                CatalogValidationUtils.SERVICE);
        CatalogValidationUtils.rejectPriceWhenNotSellable(request.getSellable(), request.getPrice(),
                CatalogValidationUtils.SERVICE);
        requireNoProvidersWhenNotPurchasable(request.getPurchasable(), request.getProviders());
        validateAssociations(request);
    }

    private void validateForUpdate(ServiceDTO request) {
        CatalogValidationUtils.validatePriceOnUpdate(request.getPrice());
        validateAssociations(request);
    }

    private void validateAssociations(ServiceDTO request) {
        CatalogValidationUtils.validateProviders(request.getProviders());
        CatalogValidationUtils.validateNoDuplicateHeadquarters(request.getHeadquarterIds());
    }

    private static void requireNoProvidersWhenNotPurchasable(Boolean purchasable,
                                                             List<AssociatedSupplierDTO> providers) {
        if (Boolean.FALSE.equals(purchasable) && providers != null && !providers.isEmpty()) {
            throw new IllegalArgumentException(
                    "Un servicio no comprable (purchasable = false) no puede tener proveedores asociados");
        }
    }

    private ServiceDomain saveOrConflict(ServiceDomain service) {
        try {
            return serviceRepository.saveAndFlush(service);
        } catch (DataIntegrityViolationException nameTakenBetweenCheckAndInsert) {
            throw new DuplicateCatalogNameException(CatalogValidationUtils.SERVICE);
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
            HeadquarterDomain headquarter = headquarterRepository.findById(headquarterId)
                    .orElseThrow(() -> new NoSuchElementException("Sede no encontrada: " + headquarterId));
            serviceHeadquarterRepository.save(new ServiceHeadquarterDomain(UUID.randomUUID(), service, headquarter));
        }
    }
    @Transactional(readOnly = true)
    public boolean isActive(UUID id) {
        return Boolean.TRUE.equals(findServiceOrThrow(id).getActive());
    }

    @Transactional(readOnly = true)
    public long countActiveServices() {
        return serviceRepository.countActiveServices();
    }

    private ServiceDomain findServiceOrThrow(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Servicio no encontrado: " + id));
    }

    private ServiceDTO toDto(ServiceDomain service) {
        List<AssociatedSupplierDTO> providers = serviceProviderRepository.findByService_Id(service.getId()).stream()
                .map(ServiceService::toSupplierDto)
                .toList();
        List<UUID> headquarterIds = serviceHeadquarterRepository.findByService_Id(service.getId()).stream()
                .map(association -> association.getHeadquarter().getId())
                .toList();
        return toDto(service, providers, headquarterIds);
    }

    private static AssociatedSupplierDTO toSupplierDto(ServiceProviderDomain association) {
        return new AssociatedSupplierDTO(association.getProvider().getId(), association.getReferencePrice());
    }

    private static ServiceDTO toDto(ServiceDomain service, List<AssociatedSupplierDTO> providers,
                                    List<UUID> headquarterIds) {
        return new ServiceDTO(
                service.getId(), service.getName(), service.getDescription(), service.getImageUrl(),
                service.getPrice(), service.getTaxCategory(), service.getSellable(), service.getActive(),
                service.getCategory(), service.getPurchasable(), providers, headquarterIds
        );
    }
}