package com.uco.ucopetapi.service.provider;

import com.uco.ucopetapi.dto.provider.ProviderDTO;

import java.util.List;
import java.util.UUID;

public interface ProviderService {

    List<ProviderDTO> findAll();

    ProviderDTO findById(UUID id);

    List<ProviderDTO> findByFilter(String providerName);

    ProviderDTO create(ProviderDTO request);

    ProviderDTO update(UUID id, ProviderDTO request);

    ProviderDTO deactivate(UUID id);

    UUID findByProviderName(String providerName);
}
