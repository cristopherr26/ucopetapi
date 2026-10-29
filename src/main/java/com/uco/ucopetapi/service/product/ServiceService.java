package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.ServiceDomain;
import com.uco.ucopetapi.domain.product.ServiceHeadquarterDomain;
import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.ServiceDTO;
import com.uco.ucopetapi.messaging.product.ServiceDeactivationSenderBroker;
import com.uco.ucopetapi.messaging.product.ServiceSaveSenderBroker;
import com.uco.ucopetapi.repository.product.IHeadquarterReferenceRepository;
import com.uco.ucopetapi.repository.product.IServiceHeadquarterRepository;
import com.uco.ucopetapi.repository.product.IServiceRepository;
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
public class ServiceService {

    private static final String ITEM = "servicio";

    private final IServiceRepository serviceRepository;
    private final IServiceHeadquarterRepository serviceHeadquarterRepository;
    private final IHeadquarterReferenceRepository headquarterRepository;
    private final ServiceSaveSenderBroker serviceSaveSenderBroker;
    private final ServiceDeactivationSenderBroker serviceDeactivationSenderBroker;

    public ServiceService(IServiceRepository serviceRepository,
                          IServiceHeadquarterRepository serviceHeadquarterRepository,
                          IHeadquarterReferenceRepository headquarterRepository,
                          ServiceSaveSenderBroker serviceSaveSenderBroker,
                          ServiceDeactivationSenderBroker serviceDeactivationSenderBroker) {
        this.serviceRepository = serviceRepository;
        this.serviceHeadquarterRepository = serviceHeadquarterRepository;
        this.headquarterRepository = headquarterRepository;
        this.serviceSaveSenderBroker = serviceSaveSenderBroker;
        this.serviceDeactivationSenderBroker = serviceDeactivationSenderBroker;
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> list(ServiceCategory category, Boolean active, Boolean purchasable, Boolean sellable,
                                 TaxCategory taxCategory, UUID headquarterId) {
        if (headquarterId != null) {
            requireHeadquarter(headquarterId);
        }
        Boolean wantedActive = active != null ? active : Boolean.TRUE;
        Map<UUID, List<UUID>> headquarters = serviceHeadquarterRepository.findAll().stream()
                .collect(Collectors.groupingBy(link -> link.getService().getId(),
                        Collectors.mapping(ServiceHeadquarterDomain::getHeadquarterId, Collectors.toList())));
        return serviceRepository.findAll().stream()
                .filter(service -> category == null || service.getCategory() == category)
                .filter(service -> wantedActive.equals(service.getActive()))
                .filter(service -> purchasable == null || purchasable.equals(service.getPurchasable()))
                .filter(service -> sellable == null || sellable.equals(service.getSellable()))
                .filter(service -> taxCategory == null || service.getTaxCategory() == taxCategory)
                .filter(service -> headquarterId == null
                        || headquarters.getOrDefault(service.getId(), List.of()).contains(headquarterId))
                .map(service -> toDTO(service, headquarters.getOrDefault(service.getId(), List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceDTO getById(UUID id) {
        ServiceDomain service = findOrThrow(id);
        List<UUID> headquarterIds = serviceHeadquarterRepository.findByService_Id(id).stream()
                .map(ServiceHeadquarterDomain::getHeadquarterId)
                .toList();
        return toDTO(service, headquarterIds);
    }

    public ServiceDTO create(@Valid ServiceDTO service) {
        validateRules(service, null);
        ServiceDTO message = toMessage(UUID.randomUUID(), service, true);
        CatalogValidationUtils.requireSaved(
                serviceSaveSenderBroker.executeAndWait(message, UUID.randomUUID().toString()).orElse(null), ITEM);
        return message;
    }

    public ServiceDTO update(UUID id, @Valid ServiceDTO service) {
        ServiceDomain current = findOrThrow(id);
        validateRules(service, id);
        ServiceDTO message = toMessage(id, service, current.getActive());
        CatalogValidationUtils.requireSaved(
                serviceSaveSenderBroker.executeAndWait(message, UUID.randomUUID().toString()).orElse(null), ITEM);
        return message;
    }

    public void deactivate(UUID id) {
        ServiceDomain current = findOrThrow(id);
        CatalogValidationUtils.requireDeactivated(serviceDeactivationSenderBroker
                .executeAndWait(toDTO(current, List.of()), UUID.randomUUID().toString()).orElse(null), ITEM);
    }

    @Transactional(readOnly = true)
    public boolean isActive(UUID id) {
        return Boolean.TRUE.equals(findOrThrow(id).getActive());
    }

    @Transactional(readOnly = true)
    public long countActiveServices() {
        return serviceRepository.countByActiveTrue();
    }

    private void validateRules(ServiceDTO service, UUID serviceId) {
        CatalogValidationUtils.validatePrice(service.getSellable(), service.getPrice(), ITEM);
        List<UUID> headquarterIds = headquarterIdsOf(service);
        CatalogValidationUtils.validateNoDuplicateHeadquarters(headquarterIds);
        headquarterIds.forEach(this::requireActiveHeadquarter);
        boolean nameTaken = serviceId == null
                ? serviceRepository.existsByNameIgnoreCase(service.getName())
                : serviceRepository.existsByNameIgnoreCaseAndIdNot(service.getName(), serviceId);
        if (nameTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un servicio con este nombre");
        }
    }

    private void requireActiveHeadquarter(UUID headquarterId) {
        requireHeadquarter(headquarterId);
        if (!headquarterRepository.existsByIdAndActiveTrue(headquarterId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La sede " + headquarterId + " está dada de baja");
        }
    }

    private void requireHeadquarter(UUID headquarterId) {
        if (!headquarterRepository.existsById(headquarterId)) {
            throw new NoSuchElementException("Sede no encontrada: " + headquarterId);
        }
    }

    private ServiceDomain findOrThrow(UUID id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Servicio no encontrado: " + id));
    }

    private static List<UUID> headquarterIdsOf(ServiceDTO service) {
        return service.getHeadquarterIds() != null ? service.getHeadquarterIds() : List.of();
    }

    private static ServiceDTO toMessage(UUID id, ServiceDTO service, boolean active) {
        return new ServiceDTO(id, service.getName(), service.getDescription(), service.getImageUrl(),
                service.getPrice(), service.getTaxCategory(), Boolean.TRUE.equals(service.getSellable()),
                active, service.getCategory(), Boolean.TRUE.equals(service.getPurchasable()), List.of(),
                headquarterIdsOf(service));
    }

    private static ServiceDTO toDTO(ServiceDomain service, List<UUID> headquarterIds) {
        return new ServiceDTO(service.getId(), service.getName(), service.getDescription(), service.getImageUrl(),
                service.getPrice(), service.getTaxCategory(), service.getSellable(), service.getActive(),
                service.getCategory(), service.getPurchasable(), List.of(), headquarterIds);
    }
}
