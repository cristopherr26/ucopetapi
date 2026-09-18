package com.uco.ucopetapi.domain.procedure.mapper;

import com.uco.ucopetapi.domain.procedure.ProcedureDomain;
import com.uco.ucopetapi.domain.procedure.ProcedureProductDomain;
import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.dto.procedure.ProcedureProductDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcedureMapper {

    public ProcedureDTO toDTO(final ProcedureDomain domain) {
        if (domain == null) {
            return null;
        }

        return new ProcedureDTO(domain.getId(), domain.getCode(), domain.getType(), domain.getDescription(),
                domain.getDurationMinutes(), domain.getSpaceId(), toProductDTOList(domain.getDefaultProducts()),
                domain.isActive());
    }

    public List<ProcedureDTO> toDTOList(final List<ProcedureDomain> domains) {
        if (domains == null) {
            return List.of();
        }

        return domains.stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ProcedureProductDomain> toProductDomainList(final List<ProcedureProductDTO> products) {
        if (products == null) {
            return List.of();
        }

        return products.stream()
                .map(product -> new ProcedureProductDomain(clean(product.getProductCode()), product.getQuantity()))
                .toList();
    }

    private List<ProcedureProductDTO> toProductDTOList(final List<ProcedureProductDomain> products) {
        if (products == null) {
            return List.of();
        }

        return products.stream()
                .map(product -> new ProcedureProductDTO(product.getProductCode(), product.getQuantity()))
                .toList();
    }

    private String clean(final String value) {
        return value == null ? null : value.trim();
    }
}
