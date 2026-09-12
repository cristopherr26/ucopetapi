package com.uco.ucopetapi.service.provider;

import com.uco.ucopetapi.domain.provider.ProviderDomain;
import com.uco.ucopetapi.dto.provider.ProviderDTO;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ProviderServiceImpl implements ProviderService {

    private final ProviderJPARepository providerJPARepository;

    public ProviderServiceImpl(ProviderJPARepository providerJPARepository) {
        this.providerJPARepository = providerJPARepository;
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
                .orElseThrow(() -> new NoSuchElementException("Proveedor no encontrado: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderDTO> findByFilter(final UUID idType, final Boolean isActive) {
        List<ProviderDomain> providers;
        if (idType != null && isActive != null) {
            providers = providerJPARepository.findByIdTypeAndActive(idType, isActive);
        } else if (idType != null) {
            providers = providerJPARepository.findByIdType(idType);
        } else if (isActive != null) {
            providers = providerJPARepository.findByActive(isActive);
        } else {
            providers = providerJPARepository.findAll();
        }
        return providers.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ProviderDTO create(ProviderDTO request) {
        UUID id = request.getId() != null ? request.getId() : UUID.randomUUID();
        ProviderDomain provider = toEntity(request, id);
        return toDto(providerJPARepository.save(provider));
    }

    @Override
    @Transactional
    public ProviderDTO update(UUID id, ProviderDTO request) {
        ProviderDomain provider = providerJPARepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Proveedor no encontrado: " + id));

        if (request.getProviderName() != null) {
            provider.setProviderName(request.getProviderName());
        }
        if (request.getRepresentName() != null) {
            provider.setRepresentName(request.getRepresentName());
        }
        if (request.getIdType() != null) {
            provider.setIdType(request.getIdType());
        }
        if (request.getDocumentNumber() != null) {
            provider.setDocumentNumber(request.getDocumentNumber());
        }
        if (request.getMobileNumber() != null) {
            provider.setMobileNumber(request.getMobileNumber());
        }
        if (request.getAddress() != null) {
            provider.setAddress(request.getAddress());
        }
        if (request.getEmail() != null) {
            provider.setEmail(request.getEmail());
        }
        provider.setActive(request.isActive());

        return toDto(providerJPARepository.save(provider));
    }

    @Override
    @Transactional
    public ProviderDTO deactivate(UUID id) {
        ProviderDomain provider = providerJPARepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Proveedor no encontrado: " + id));
        provider.setActive(false);
        return toDto(providerJPARepository.save(provider));
    }

    private ProviderDTO toDto(final ProviderDomain provider) {
        return new ProviderDTO(
                provider.getId(),
                provider.getProviderName(),
                provider.getRepresentName(),
                provider.getIdType(),
                provider.getDocumentNumber(),
                provider.getMobileNumber(),
                provider.getAddress(),
                provider.getEmail(),
                provider.isActive()
        );
    }

    private ProviderDomain toEntity(final ProviderDTO request, final UUID id) {
        return new ProviderDomain(
                id,
                request.getProviderName(),
                request.getRepresentName(),
                request.getIdType(),
                request.getDocumentNumber(),
                request.getMobileNumber(),
                request.getAddress(),
                request.getEmail(),
                request.isActive()
        );
    }
}
