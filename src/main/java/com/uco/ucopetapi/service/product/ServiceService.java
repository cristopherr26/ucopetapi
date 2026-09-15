package com.uco.ucopetapi.service.product;

import com.uco.ucopetapi.domain.product.enums.ServiceCategory;
import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.dto.product.ServiceDTO;

import java.util.List;
import java.util.UUID;

public interface ServiceService {

    List<ServiceDTO> list(ServiceCategory category, Boolean active, Boolean purchasable, Boolean sellable,
                          TaxCategory taxCategory, UUID headquarterId);

    ServiceDTO getById(UUID id);

    ServiceDTO create(ServiceDTO request);

    ServiceDTO update(UUID id, ServiceDTO request);

    void deactivate(UUID id);
}