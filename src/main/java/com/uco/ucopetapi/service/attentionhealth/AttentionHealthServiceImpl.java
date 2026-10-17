package com.uco.ucopetapi.service.attentionhealth;

import com.uco.ucopetapi.domain.attentionhealth.AttentionHealthDomain;
import com.uco.ucopetapi.domain.attentionhealth.AttentionHealthProductDomain;
import com.uco.ucopetapi.domain.order.OrderDomain;
import com.uco.ucopetapi.domain.order.mapper.OrderMapper;
import com.uco.ucopetapi.dto.attentionhealth.AttentionHealthDTO;
import com.uco.ucopetapi.dto.attentionhealth.AttentionHealthVitalSignsDTO;
import com.uco.ucopetapi.dto.attentionhealth.AttentionHealthProductDTO;
import com.uco.ucopetapi.dto.order.OrderDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.dto.sale.AddItemSaleRequestDTO;
import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import com.uco.ucopetapi.dto.sale.enums.ItemType;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.repository.attentionhealth.IAttentionHealthRepository;
import com.uco.ucopetapi.service.headquarter.HeadquarterService;
import com.uco.ucopetapi.service.order.IOrderService;
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.procedure.ProcedureService;
import com.uco.ucopetapi.service.sale.ItemSaleService;
import com.uco.ucopetapi.service.sale.SaleOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Application service for the new attention aggregate.
 * External references are validated for presence and are not duplicated here.
 */
@Service
public class AttentionHealthServiceImpl implements AttentionHealthService {

    private static final String REGISTERED = "REGISTERED";
    private static final String IN_PROGRESS = "IN_PROGRESS";
    private static final String CANCELLED = "CANCELLED";
    private static final String COMPLETED = "COMPLETED";
    private static final String ATTENTION_NOT_FOUND =
            "No se encontró la atención con el identificador: ";
    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");
    private static final int MAX_PRODUCT_REFERENCES = 100;
    private static final int MAX_PROCEDURE_REFERENCES = 100;

    private final IAttentionHealthRepository attentionHealthRepository;
    private final ProcedureService procedureService;
    private final IOrderService orderService;
    private final OrderMapper orderMapper;
    private final ProductService productService;
    private final HeadquarterService headquarterService;
    private final SaleOrderService saleOrderService;
    private final ItemSaleService itemSaleService;

    public AttentionHealthServiceImpl(final IAttentionHealthRepository attentionHealthRepository,
                                   final ProcedureService procedureService,
                                   final IOrderService orderService,
                                   final OrderMapper orderMapper,
                                   final ProductService productService,
                                   final HeadquarterService headquarterService,
                                   final SaleOrderService saleOrderService,
                                   final ItemSaleService itemSaleService) {
        this.attentionHealthRepository = attentionHealthRepository;
        this.procedureService = procedureService;
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.productService = productService;
        this.headquarterService = headquarterService;
        this.saleOrderService = saleOrderService;
        this.itemSaleService = itemSaleService;
    }

    @Override
    @Transactional
    public AttentionHealthDTO create(final AttentionHealthDTO request) {
        validateRequest(request);

        if (request.getAppointmentId() != null) {
            AttentionHealthDomain existing = attentionHealthRepository
                    .findByAppointmentId(request.getAppointmentId())
                    .orElse(null);
            if (existing != null) {
                validateIdempotentRequest(existing, request);
                return toDto(existing);
            }
        }

        AttentionHealthDomain attention = new AttentionHealthDomain();
        if (!REGISTERED.equalsIgnoreCase(request.getStatus().trim())) {
            throw new BusinessException("Una atención nueva debe iniciar en estado REGISTERED.");
        }
        updateDomain(attention, request);
        return toDto(attentionHealthRepository.save(attention));
    }

    @Override
    @Transactional(readOnly = true)
    public AttentionHealthDTO findById(final Long attentionId) {
        validateAttentionId(attentionId);
        return attentionHealthRepository.findById(attentionId)
                .map(this::toDto)
                .orElseThrow(() -> new BusinessException(
                        ATTENTION_NOT_FOUND + attentionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttentionHealthDTO> findByPetId(final UUID petId) {
        if (petId == null) {
            throw new BusinessException("El identificador de la mascota es obligatorio.");
        }
        return attentionHealthRepository.findByPetIdOrderByAttentionDateDesc(petId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AttentionHealthDTO update(final Long attentionId, final AttentionHealthDTO request) {
        validateAttentionId(attentionId);
        validateRequest(request);

        AttentionHealthDomain attention = attentionHealthRepository.findById(attentionId)
                .orElseThrow(() -> new BusinessException(
                        ATTENTION_NOT_FOUND + attentionId));

        validateImmutableReferences(attention, request);
        requireStatus(attention, REGISTERED, IN_PROGRESS,
                "Solo se puede actualizar una atención registrada o en curso.");
        if (!attention.getStatus().equalsIgnoreCase(request.getStatus().trim())) {
            throw new BusinessException(
                    "El cambio de estado debe realizarse mediante la operación correspondiente.");
        }
        updateDomain(attention, request);
        return toDto(attentionHealthRepository.save(attention));
    }

    @Override
    @Transactional
    public AttentionHealthDTO start(final Long attentionId) {
        AttentionHealthDomain attention = findDomain(attentionId);
        requireStatus(attention, REGISTERED, "Solo se puede iniciar una atención registrada.");
        attention.setStatus(IN_PROGRESS);
        return toDto(attentionHealthRepository.save(attention));
    }

    @Override
    @Transactional
    public AttentionHealthDTO cancel(final Long attentionId) {
        AttentionHealthDomain attention = findDomain(attentionId);
        requireStatus(attention, REGISTERED, IN_PROGRESS,
                "Solo se puede cancelar una atención registrada o en curso.");
        attention.setStatus(CANCELLED);
        return toDto(attentionHealthRepository.save(attention));
    }

    @Override
    @Transactional
    public AttentionHealthDTO finish(final Long attentionId) {
        AttentionHealthDomain attention = findDomain(attentionId);
        requireStatus(attention, IN_PROGRESS, "Solo se puede finalizar una atención en curso.");
        validateStoredAttentionBeforeFinish(attention);
        attention.setStatus(COMPLETED);
        AttentionHealthDomain savedAttention = attentionHealthRepository.saveAndFlush(attention);
        createMedicalOrders(savedAttention);
        createProductSale(savedAttention);
        return toDto(savedAttention);
    }

    private void validateRequest(final AttentionHealthDTO request) {
        if (request == null) {
            throw new BusinessException("La información de la atención es obligatoria.");
        }
        requireUuid(request.getPetId(), "El identificador de la mascota es obligatorio.");
        requireUuid(request.getTutorId(), "El identificador del tutor es obligatorio.");
        requireUuid(request.getDoctorId(), "El identificador del médico es obligatorio.");
        requireUuid(request.getHeadquarterId(), "El identificador de la sede es obligatorio.");
        validateHeadquarter(request.getHeadquarterId());
        requireText(request.getAttentionType(), 80, "El tipo de atención es obligatorio.");
        requireText(request.getStatus(), 30, "El estado de la atención es obligatorio.");
        validateStatus(request.getStatus());
        requireDate(request.getAttentionDate());
        validateOptionalText(request.getReason(), 500, "El motivo de consulta");
        validateOptionalText(request.getClinicalAssessment(), 2000, "La valoración clínica");
        validateOptionalText(request.getObservations(), 1000, "Las observaciones");
        validateOptionalText(request.getDiagnosis(), 1000, "El diagnóstico");
        validateProducts(request.getProducts(), request.getHeadquarterId());
        validateReferences(request.getProcedureIds(), MAX_PROCEDURE_REFERENCES, "procedimientos");
        validateProcedures(request.getProcedureIds());
        validateVitalSigns(request.getVitalSigns());
    }

    private void validateReferences(final List<UUID> references, final int maxSize, final String name) {
        if (references == null || references.size() > maxSize || references.stream().anyMatch(Objects::isNull)
                || references.stream().distinct().count() != references.size()) {
            throw new BusinessException("La lista de " + name + " contiene referencias inválidas.");
        }
    }

    private void validateVitalSigns(final AttentionHealthVitalSignsDTO vitalSigns) {
        if (vitalSigns == null) {
            return;
        }
        validatePositive(vitalSigns.getTemperature(), "La temperatura");
        validatePositive(vitalSigns.getWeight(), "El peso");
        validateNonNegative(vitalSigns.getHeartRate(), "La frecuencia cardíaca");
        validateNonNegative(vitalSigns.getRespiratoryRate(), "La frecuencia respiratoria");
        validateNonNegative(vitalSigns.getSystolicPressure(), "La presión sistólica");
        validateNonNegative(vitalSigns.getDiastolicPressure(), "La presión diastólica");
        if (vitalSigns.getBodyConditionScore() != null
                && (vitalSigns.getBodyConditionScore() < 0 || vitalSigns.getBodyConditionScore() > 9)) {
            throw new BusinessException("La condición corporal debe estar entre 0 y 9.");
        }
        if (vitalSigns.getMeasurementDate() != null
                && vitalSigns.getMeasurementDate().isAfter(LocalDateTime.now(ZONE_ID))) {
            throw new BusinessException("La fecha de medición no puede estar en el futuro.");
        }
    }

    private void validateNonNegative(final Number value, final String fieldName) {
        if (value != null && (!Double.isFinite(value.doubleValue()) || value.doubleValue() < 0)) {
            throw new BusinessException(fieldName + " no puede ser negativa.");
        }
    }

    private void validatePositive(final Number value, final String fieldName) {
        if (value != null && (!Double.isFinite(value.doubleValue()) || value.doubleValue() <= 0)) {
            throw new BusinessException(fieldName + " debe ser mayor que cero.");
        }
    }

    private void validateIdempotentRequest(final AttentionHealthDomain existing, final AttentionHealthDTO request) {
        if (!existing.getPetId().equals(request.getPetId())) {
            throw new BusinessException("La cita ya está asociada a otra mascota.");
        }
    }

    private void validateStatus(final String status) {
        String normalizedStatus = status.trim().toUpperCase();
        if (!REGISTERED.equals(normalizedStatus) && !IN_PROGRESS.equals(normalizedStatus)
                && !CANCELLED.equals(normalizedStatus) && !COMPLETED.equals(normalizedStatus)) {
            throw new BusinessException("El estado de la atención no es válido.");
        }
    }

    private void validateStoredAttentionBeforeFinish(final AttentionHealthDomain attention) {
        requireUuid(attention.getPetId(), "La atención no tiene una mascota asociada.");
        requireUuid(attention.getTutorId(), "La atención no tiene un tutor asociado.");
        requireUuid(attention.getDoctorId(), "La atención no tiene un médico asociado.");
        requireDate(attention.getAttentionDate());
        requireText(attention.getAttentionType(), 80, "La atención no tiene un tipo válido.");
        validateProducts(attention.getProducts(), attention.getHeadquarterId());
        validateStoredReferences(attention.getProcedureIds(), MAX_PROCEDURE_REFERENCES, "procedimientos");
        validateProcedures(attention.getProcedureIds());
        validateStoredVitalSigns(attention);
    }

    private void validateStoredReferences(final List<UUID> references, final int maxSize,
                                          final String name) {
        validateReferences(references, maxSize, name);
    }

    private void validateProcedures(final List<UUID> procedureIds) {
        for (UUID procedureId : procedureIds) {
            try {
                if (!Boolean.TRUE.equals(procedureService.findById(procedureId).getActive())) {
                    throw new BusinessException(
                            "El procedimiento " + procedureId + " no se encuentra activo.");
                }
            } catch (BusinessException _) {
                throw new BusinessException(
                        "El procedimiento " + procedureId + " no existe o no puede utilizarse.");
            }
        }
    }

    private void validateProducts(final List<?> products, final UUID headquarterId) {
        if (products == null || products.size() > MAX_PRODUCT_REFERENCES) {
            throw new BusinessException("La lista de productos es inválida.");
        }
        Set<UUID> productIds = new HashSet<>();
        for (Object productReference : products) {
            UUID productId;
            Integer quantity;
            if (productReference instanceof AttentionHealthProductDTO product) {
                productId = product.getProductId();
                quantity = product.getQuantity();
            } else {
                AttentionHealthProductDomain product = (AttentionHealthProductDomain) productReference;
                productId = product.getProductId();
                quantity = product.getQuantity();
            }
            requireUuid(productId, "El identificador del producto es obligatorio.");
            if (!productIds.add(productId)) {
                throw new BusinessException("Un producto solo puede asociarse una vez por atención.");
            }
            if (quantity == null || quantity <= 0) {
                throw new BusinessException("La cantidad del producto debe ser mayor que cero.");
            }
            try {
                productService.getById(productId, headquarterId);
            } catch (RuntimeException _) {
                throw new BusinessException("El producto " + productId + " no existe.");
            }
        }
    }

    private void validateHeadquarter(final UUID headquarterId) {
        if (!Boolean.TRUE.equals(headquarterService.findById(headquarterId).getIsActive())) {
            throw new BusinessException("La sede no está activa.");
        }
    }

    private void validateStoredVitalSigns(final AttentionHealthDomain attention) {
        AttentionHealthVitalSignsDTO vitalSigns = new AttentionHealthVitalSignsDTO();
        vitalSigns.setTemperature(attention.getTemperature());
        vitalSigns.setHeartRate(attention.getHeartRate());
        vitalSigns.setRespiratoryRate(attention.getRespiratoryRate());
        vitalSigns.setSystolicPressure(attention.getSystolicPressure());
        vitalSigns.setDiastolicPressure(attention.getDiastolicPressure());
        vitalSigns.setWeight(attention.getWeight());
        vitalSigns.setBodyConditionScore(attention.getBodyConditionScore());
        vitalSigns.setMeasurementDate(attention.getMeasurementDate());
        validateVitalSigns(vitalSigns);
    }

    private void createMedicalOrders(final AttentionHealthDomain attention) {
        for (UUID procedureId : attention.getProcedureIds()) {
            OrderDTO orderRequest = new OrderDTO();
            orderRequest.setPetId(attention.getPetId());
            orderRequest.setProcedureId(procedureId);

            OrderDomain order = orderService.save(orderMapper.toDomain(orderRequest));
            if (order == null || order.getId() == null) {
                throw new BusinessException(
                        "No fue posible crear la orden médica para el procedimiento " + procedureId + ".");
            }
        }
    }

    private void createProductSale(final AttentionHealthDomain attention) {
        if (attention.getProducts().isEmpty()) {
            return;
        }

        SaleOrderDTO saleRequest = new SaleOrderDTO();
        saleRequest.setHeadquarterId(attention.getHeadquarterId());
        saleRequest.setClientID(attention.getTutorId());
        saleRequest.setPetId(attention.getPetId());
        saleRequest.setComment("Productos asociados a la atención " + attention.getAttentionId());
        SaleOrderDTO sale = saleOrderService.registerNewSaleOrder(saleRequest);

        for (AttentionHealthProductDomain productReference : attention.getProducts()) {
            UUID productId = productReference.getProductId();
            ProductDTO product = productService.getById(productId, attention.getHeadquarterId());
            AddItemSaleRequestDTO item = new AddItemSaleRequestDTO();
            item.setItemId(productId);
            item.setItemType(ItemType.PRODUCTO);
            item.setQuantity(productReference.getQuantity());
            item.setUnitPrice(product.getPrice());
            itemSaleService.addItemToSaleOrder(sale.getId(), item);
        }
    }

    private void requireStatus(final AttentionHealthDomain attention, final String expectedStatus,
                               final String message) {
        requireStatus(attention, new String[]{expectedStatus}, message);
    }

    private void requireStatus(final AttentionHealthDomain attention, final String firstStatus,
                               final String secondStatus, final String message) {
        requireStatus(attention, new String[]{firstStatus, secondStatus}, message);
    }

    private void requireStatus(final AttentionHealthDomain attention, final String[] expectedStatuses,
                               final String message) {
        if (attention.getStatus() == null) {
            throw new BusinessException("La atención no tiene un estado válido.");
        }
        for (String expectedStatus : expectedStatuses) {
            if (expectedStatus.equalsIgnoreCase(attention.getStatus())) {
                return;
            }
        }
        throw new BusinessException(message);
    }

    private AttentionHealthDomain findDomain(final Long attentionId) {
        validateAttentionId(attentionId);
        return attentionHealthRepository.findById(attentionId)
                .orElseThrow(() -> new BusinessException(
                        ATTENTION_NOT_FOUND + attentionId));
    }

    private void validateImmutableReferences(final AttentionHealthDomain existing,
                                             final AttentionHealthDTO request) {
        if (!existing.getPetId().equals(request.getPetId())
                || !existing.getTutorId().equals(request.getTutorId())
                || !existing.getDoctorId().equals(request.getDoctorId())
                || !existing.getHeadquarterId().equals(request.getHeadquarterId())
                || !sameReference(existing.getAppointmentId(), request.getAppointmentId())) {
            throw new BusinessException(
                    "Las referencias de mascota, tutor, médico y cita no pueden cambiarse.");
        }
    }

    private boolean sameReference(final UUID first, final UUID second) {
        return first == null ? second == null : first.equals(second);
    }

    private void updateDomain(final AttentionHealthDomain domain, final AttentionHealthDTO request) {
        domain.setPetId(request.getPetId());
        domain.setTutorId(request.getTutorId());
        domain.setDoctorId(request.getDoctorId());
        domain.setAppointmentId(request.getAppointmentId());
        domain.setHeadquarterId(request.getHeadquarterId());
        domain.setAttentionDate(request.getAttentionDate());
        domain.setAttentionType(request.getAttentionType().trim());
        domain.setReason(request.getReason());
        domain.setClinicalAssessment(request.getClinicalAssessment());
        domain.setObservations(request.getObservations());
        domain.setDiagnosis(request.getDiagnosis());
        domain.setStatus(request.getStatus().trim().toUpperCase());
        domain.setProducts(request.getProducts().stream()
                .map(product -> new AttentionHealthProductDomain(product.getProductId(), product.getQuantity()))
                .toList());
        domain.setProcedureIds(request.getProcedureIds());
        setVitalSigns(domain, request.getVitalSigns());
    }

    private void setVitalSigns(final AttentionHealthDomain domain, final AttentionHealthVitalSignsDTO vitalSigns) {
        if (vitalSigns == null) {
            domain.setTemperature(null);
            domain.setHeartRate(null);
            domain.setRespiratoryRate(null);
            domain.setSystolicPressure(null);
            domain.setDiastolicPressure(null);
            domain.setWeight(null);
            domain.setBodyConditionScore(null);
            domain.setMeasurementDate(null);
            return;
        }
        domain.setTemperature(vitalSigns.getTemperature());
        domain.setHeartRate(vitalSigns.getHeartRate());
        domain.setRespiratoryRate(vitalSigns.getRespiratoryRate());
        domain.setSystolicPressure(vitalSigns.getSystolicPressure());
        domain.setDiastolicPressure(vitalSigns.getDiastolicPressure());
        domain.setWeight(vitalSigns.getWeight());
        domain.setBodyConditionScore(vitalSigns.getBodyConditionScore());
        domain.setMeasurementDate(vitalSigns.getMeasurementDate());
    }

    private AttentionHealthDTO toDto(final AttentionHealthDomain domain) {
        AttentionHealthDTO dto = new AttentionHealthDTO();
        dto.setAttentionId(domain.getAttentionId());
        dto.setPetId(domain.getPetId());
        dto.setTutorId(domain.getTutorId());
        dto.setDoctorId(domain.getDoctorId());
        dto.setAppointmentId(domain.getAppointmentId());
        dto.setHeadquarterId(domain.getHeadquarterId());
        dto.setAttentionDate(domain.getAttentionDate());
        dto.setAttentionType(domain.getAttentionType());
        dto.setReason(domain.getReason());
        dto.setClinicalAssessment(domain.getClinicalAssessment());
        dto.setObservations(domain.getObservations());
        dto.setDiagnosis(domain.getDiagnosis());
        dto.setStatus(domain.getStatus());
        dto.setProducts(domain.getProducts().stream()
                .map(product -> {
                    AttentionHealthProductDTO item = new AttentionHealthProductDTO();
                    item.setProductId(product.getProductId());
                    item.setQuantity(product.getQuantity());
                    return item;
                })
                .toList());
        dto.setProcedureIds(domain.getProcedureIds());

        AttentionHealthVitalSignsDTO vitalSigns = new AttentionHealthVitalSignsDTO();
        vitalSigns.setTemperature(domain.getTemperature());
        vitalSigns.setHeartRate(domain.getHeartRate());
        vitalSigns.setRespiratoryRate(domain.getRespiratoryRate());
        vitalSigns.setSystolicPressure(domain.getSystolicPressure());
        vitalSigns.setDiastolicPressure(domain.getDiastolicPressure());
        vitalSigns.setWeight(domain.getWeight());
        vitalSigns.setBodyConditionScore(domain.getBodyConditionScore());
        vitalSigns.setMeasurementDate(domain.getMeasurementDate());
        dto.setVitalSigns(vitalSigns);
        return dto;
    }

    private void validateAttentionId(final Long attentionId) {
        if (attentionId == null || attentionId <= 0) {
            throw new BusinessException("El identificador de la atención debe ser positivo.");
        }
    }

    private void requireUuid(final UUID value, final String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
    }

    private void requireDate(final LocalDateTime value) {
        if (value == null) {
            throw new BusinessException("La fecha y hora de la atención son obligatorias.");
        }
        if (value.isAfter(LocalDateTime.now(ZONE_ID))) {
            throw new BusinessException("La fecha de la atención no puede estar en el futuro.");
        }
    }

    private void requireText(final String value, final int maxLength, final String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(message);
        }
        if (value.trim().length() > maxLength) {
            throw new BusinessException("El texto supera la longitud máxima permitida.");
        }
    }

    private void validateOptionalText(final String value, final int maxLength, final String fieldName) {
        if (value != null && value.trim().length() > maxLength) {
            throw new BusinessException(fieldName + " supera la longitud máxima permitida.");
        }
    }
}


