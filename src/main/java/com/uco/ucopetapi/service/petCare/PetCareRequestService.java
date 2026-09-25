package com.uco.ucopetapi.service.petCare;

import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.domain.order.mapper.OrderMapper;
import com.uco.ucopetapi.domain.petCare.PetCareRequestDomain;
import com.uco.ucopetapi.dto.attention.CancellationRequest;
import com.uco.ucopetapi.dto.order.OrderDTO;
import com.uco.ucopetapi.dto.petCare.CreatePetCareRequestDTO;
import com.uco.ucopetapi.dto.petCare.PetCareRequestDTO;
import com.uco.ucopetapi.dto.petCare.PetCareRequestStatus;
import com.uco.ucopetapi.dto.petCare.PetCareRequestType;
import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.exception.clinical.ClinicalException;
import com.uco.ucopetapi.repository.petCare.PetCareRequestRepository;
import com.uco.ucopetapi.service.attention.CurrentDoctor;
import com.uco.ucopetapi.service.attention.CurrentDoctorProvider;
import com.uco.ucopetapi.service.attention.OpenPetCare;
import com.uco.ucopetapi.service.attention.OpenPetCareLoader;
import com.uco.ucopetapi.service.order.IOrderService;
import com.uco.ucopetapi.service.procedure.ProcedureService;
import com.uco.ucopetapi.service.product.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class PetCareRequestService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");

    private final PetCareRequestRepository petCareRequestRepository;
    private final CurrentDoctorProvider currentDoctorProvider;
    private final OpenPetCareLoader openPetCareLoader;
    private final ProductService productService;
    private final ProcedureService procedureService;
    private final OrderMapper orderMapper;
    private final IOrderService orderService;

    public PetCareRequestService(final PetCareRequestRepository petCareRequestRepository,
                                 final CurrentDoctorProvider currentDoctorProvider,
                                 final OpenPetCareLoader openPetCareLoader,
                                 final ProductService productService,
                                 final ProcedureService procedureService,
                                 final OrderMapper orderMapper,
                                 final IOrderService orderService) {
        this.petCareRequestRepository = petCareRequestRepository;
        this.currentDoctorProvider = currentDoctorProvider;
        this.openPetCareLoader = openPetCareLoader;
        this.productService = productService;
        this.procedureService = procedureService;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
    }

    @Transactional
    public PetCareRequestDTO create(final UUID petCareId, final CreatePetCareRequestDTO request) {
        CreatePetCareRequestDTO data = request == null ? new CreatePetCareRequestDTO() : request;
        CurrentDoctor doctor = currentDoctorProvider.get();
        OpenPetCare openPetCare = openPetCareLoader.load(petCareId);

        PetCareRequestDomain entity = new PetCareRequestDomain();
        entity.setId(UUID.randomUUID());
        entity.setPetCareId(openPetCare.getPetCareId());
        entity.setRequestType(data.getType());
        entity.setNotes(data.getNotes());
        entity.setRequestedByDoctorId(doctor.getDoctorId());
        entity.setRequestedByName(doctor.getFullName());
        entity.setRequestedAt(LocalDateTime.now(ZONE_ID));
        entity.setStatus(PetCareRequestStatus.REQUESTED);

        if (data.getType() == PetCareRequestType.MEDICATION) {
            applyMedication(entity, data);
        } else if (data.getType() == PetCareRequestType.PROCEDURE) {
            applyProcedure(entity, data, openPetCare.getPetId());
        }

        entity.validate();
        return toDto(petCareRequestRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<PetCareRequestDTO> findByPetCare(final UUID petCareId) {
        openPetCareLoader.ensureExists(petCareId);
        return petCareRequestRepository.findByPetCareIdOrderByRequestedAtAsc(petCareId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PetCareRequestDTO cancel(final UUID requestId, final CancellationRequest request) {
        CurrentDoctor doctor = currentDoctorProvider.get();
        PetCareRequestDomain entity = petCareRequestRepository.findById(requestId)
                .orElseThrow(() -> ClinicalException.notFound("No se encontró la solicitud indicada"));
        entity.cancel(doctor.getDoctorId(), request == null ? null : request.getReason());
        if (entity.getOrderId() != null) {
            orderService.delete(entity.getOrderId());
        }
        entity.validate();
        return toDto(petCareRequestRepository.save(entity));
    }

    private void applyMedication(final PetCareRequestDomain entity, final CreatePetCareRequestDTO request) {
        entity.setQuantity(request.getQuantity());
        if (request.getProductId() == null) {
            return;
        }

        ProductDTO product = productService.getById(request.getProductId(), null);
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw ClinicalException.badRequest("El producto no está activo");
        }
        entity.setProductId(product.getId());
        entity.setItemName(product.getName());
    }

    private void applyProcedure(final PetCareRequestDomain entity, final CreatePetCareRequestDTO request,
                                final UUID petId) {
        if (request.getProcedureId() == null) {
            return;
        }

        ProcedureDTO procedure = procedureService.findById(request.getProcedureId());
        if (!Boolean.TRUE.equals(procedure.getActive())) {
            throw ClinicalException.badRequest("El procedimiento no está activo");
        }

        OrderDTO orderDto = new OrderDTO();
        orderDto.setPetId(petId);
        orderDto.setProcedureId(procedure.getId());
        OrderDomain savedOrder = orderService.save(orderMapper.toDomain(orderDto));

        String code = procedure.getCode() == null ? "" : procedure.getCode();
        String description = procedure.getDescription() == null ? "" : procedure.getDescription();
        entity.setProcedureId(procedure.getId());
        entity.setOrderId(savedOrder.getId());
        entity.setItemName(code + " - " + description);
        entity.setQuantity(1);
    }

    private PetCareRequestDTO toDto(final PetCareRequestDomain entity) {
        return new PetCareRequestDTO(
                entity.getId(),
                entity.getPetCareId(),
                entity.getRequestType(),
                entity.getProductId(),
                entity.getProcedureId(),
                entity.getItemName(),
                entity.getQuantity(),
                entity.getNotes(),
                entity.getOrderId(),
                entity.getRequestedByDoctorId(),
                entity.getRequestedByName(),
                entity.getRequestedAt(),
                entity.getStatus(),
                entity.getCancellationReason()
        );
    }
}
