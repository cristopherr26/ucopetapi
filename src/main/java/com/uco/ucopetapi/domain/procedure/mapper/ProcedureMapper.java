package com.uco.ucopetapi.domain.procedure.mapper;

import com.uco.ucopetapi.domain.procedure.ProcedureDomain;
import com.uco.ucopetapi.domain.procedure.ProcedureItemDomain;
import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.dto.procedure.ProcedureItemDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcedureMapper {

    public ProcedureDTO toDTO(final ProcedureDomain domain) {
        if (domain == null) {
            return null;
        }

        return new ProcedureDTO(domain.getId(), domain.getCode(), domain.getType(), domain.getDescription(),
                domain.getDurationMinutes(), domain.getSpaceId(), toItemDTOList(domain.getDefaultItems()),
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

    private List<ProcedureItemDTO> toItemDTOList(final List<ProcedureItemDomain> items) {
        if (items == null) {
            return List.of();
        }

        return items.stream()
                .map(item -> new ProcedureItemDTO(item.getItemId(), item.getQuantity()))
                .toList();
    }
}