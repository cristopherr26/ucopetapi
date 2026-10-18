package com.uco.ucopetapi.service.provider;

import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.domain.provider.ProviderValidationException;
import com.uco.ucopetapi.dto.provider.ProviderDTO;
import com.uco.ucopetapi.messages.provider.ProviderSavePublisher;
import com.uco.ucopetapi.messages.provider.ProviderDeactivatePublisher;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProviderServiceImpl implements ProviderService {

    private static final String PROVIDER_NOT_FOUND_MSG = "Proveedor no encontrado: ";

    private final ProviderJPARepository providerJPARepository;
    private final ProviderSavePublisher providerSavePublisher;
    private final ProviderDeactivatePublisher providerDeactivatePublisher;

    public ProviderServiceImpl(ProviderJPARepository providerJPARepository,
                               ProviderSavePublisher providerSavePublisher,
                               ProviderDeactivatePublisher providerDeactivatePublisher) {
        this.providerJPARepository = providerJPARepository;
        this.providerSavePublisher = providerSavePublisher;
        this.providerDeactivatePublisher = providerDeactivatePublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderDTO> findAll() {
        return providerJPARepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDTO findById(UUID id) {
        return providerJPARepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException(PROVIDER_NOT_FOUND_MSG + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderDTO> findByFilter(final String providerName) {
        List<ProviderDomain> providers = (providerName != null && !providerName.isBlank())
                ? providerJPARepository.findByProviderNameContainingIgnoreCase(providerName)
                : providerJPARepository.findAll();

        return providers.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findByProviderName(final String providerName) {
        return providerJPARepository.findByProviderName(providerName)
                .map(ProviderDomain::getId)
                .orElseThrow(() -> new NoSuchElementException(PROVIDER_NOT_FOUND_MSG + providerName));
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDTO create(ProviderDTO request) {
        UUID id = request.getId() != null ? request.getId() : UUID.randomUUID();
        ProviderDomain provider = ProviderDomain.builder()
                .id(id)
                .providerName(request.getProviderName())
                .representName(request.getRepresentName())
                .idType(request.getIdType())
                .documentNumber(request.getDocumentNumber())
                .mobileNumber(request.getMobileNumber())
                .address(request.getAddress())
                .email(request.getEmail())
                .active(true)
                .build();

        if (providerJPARepository.existsByIdTypeAndDocumentNumber(
                provider.getIdType(), provider.getDocumentNumber())) {
            throw new ProviderValidationException(
                    "Ya existe un proveedor con ese tipo y número de documento");
        }
        ProviderDTO dto = toDto(provider);
        providerSavePublisher.publish(dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDTO update(UUID id, ProviderDTO request) {
        ProviderDomain current = providerJPARepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(PROVIDER_NOT_FOUND_MSG + id));
        if (!current.isActive()) {
            throw new ProviderValidationException("No se puede editar un proveedor inactivo");
        }

        ProviderDomain updated = ProviderDomain.builder()
                .id(current.getId())
                .providerName(request.getProviderName())
                .representName(request.getRepresentName())
                .idType(request.getIdType())
                .documentNumber(request.getDocumentNumber())
                .mobileNumber(request.getMobileNumber())
                .address(request.getAddress())
                .email(request.getEmail())
                .active(true)
                .build();

        if (providerJPARepository.existsByIdTypeAndDocumentNumberAndIdNot(
                updated.getIdType(), updated.getDocumentNumber(), id)) {
            throw new ProviderValidationException(
                    "Ya existe un proveedor con ese tipo y número de documento");
        }
        ProviderDTO dto = toDto(updated);
        providerSavePublisher.publish(dto);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderDTO deactivate(UUID id) {
        ProviderDomain current = providerJPARepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(PROVIDER_NOT_FOUND_MSG + id));
        if (!current.isActive()) {
            throw new ProviderValidationException("El proveedor ya está inactivo");
        }
        ProviderDTO dto = toDto(current);
        providerDeactivatePublisher.publish(dto);
        return ProviderDTO.builder()
                .id(dto.getId())
                .providerName(dto.getProviderName())
                .representName(dto.getRepresentName())
                .idType(dto.getIdType())
                .documentNumber(dto.getDocumentNumber())
                .mobileNumber(dto.getMobileNumber())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .isActive(false)
                .build();
    }

    private ProviderDTO toDto(final ProviderDomain provider) {
        return ProviderDTO.builder()
                .id(provider.getId())
                .providerName(provider.getProviderName())
                .representName(provider.getRepresentName())
                .idType(provider.getIdType())
                .documentNumber(provider.getDocumentNumber())
                .mobileNumber(provider.getMobileNumber())
                .address(provider.getAddress())
                .email(provider.getEmail())
                .isActive(provider.isActive())
                .build();
    }

}
