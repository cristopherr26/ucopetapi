package com.uco.ucopetapi.domain.order.mapper;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.domain.pet.PetDomain;
import com.uco.ucopetapi.domain.procedure.ProcedureDomain;
import com.uco.ucopetapi.dto.order.OrderDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    @PersistenceContext
    private EntityManager entityManager;

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
                domain.getIsAuthorized()
        );
    }

    public OrderDomain toDomain(OrderDTO dto) {
        if (dto == null) return null;

        PetDomain pet = dto.getPetId() != null
                ? entityManager.getReference(PetDomain.class, dto.getPetId())
                : null;
        ProcedureDomain procedure = dto.getProcedureId() != null
                ? entityManager.getReference(ProcedureDomain.class, dto.getProcedureId())
                : null;

        return new OrderDomain(
                dto.getId(),
                dto.getIdOrder(),
                dto.getTutorId(),
                pet,
                procedure,
                dto.getState(),
                dto.getDate(),
                dto.getIsAuthorized()
        );
    }
}