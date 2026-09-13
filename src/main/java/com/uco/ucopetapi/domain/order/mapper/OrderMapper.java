package com.uco.ucopetapi.domain.order.mapper;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.dto.order.OrderDTO;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderDTO toDTO(OrderDomain domain) {
        if (domain == null) return null;

        return new OrderDTO(
                domain.getId(),
                domain.getIdOrder(),
                domain.getTutorId(),
                domain.getPetId(),
                domain.getProcedureId(),
                domain.getState(),
                domain.getDate(),
                domain.getAuthorized()
        );
    }

    public OrderDomain toDomain(OrderDTO dto) {
        if (dto == null) return null;

        return new OrderDomain(
                dto.getId(),
                dto.getIdOrder(),
                dto.getTutorId(),
                dto.getPetId(),
                dto.getProcedureId(),
                dto.getState(),
                dto.getDate(),
                dto.getAuthorized()
        );
    }
}