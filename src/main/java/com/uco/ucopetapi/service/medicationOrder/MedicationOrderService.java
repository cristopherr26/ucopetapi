package com.uco.ucopetapi.service.medicationOrder;

import com.uco.ucopetapi.domain.medicationOrder.MedicationOrderDomain;
import com.uco.ucopetapi.domain.medicationOrder.MedicationOrderItemDomain;
import com.uco.ucopetapi.dto.attention.CancellationRequest;
import com.uco.ucopetapi.dto.medicationOrder.CreateMedicationOrderRequest;
import com.uco.ucopetapi.dto.medicationOrder.MedicationOrderDTO;
import com.uco.ucopetapi.dto.medicationOrder.MedicationOrderItemDTO;
import com.uco.ucopetapi.dto.medicationOrder.MedicationOrderItemRequest;
import com.uco.ucopetapi.dto.medicationOrder.MedicationOrderSignatureDTO;
import com.uco.ucopetapi.dto.medicationOrder.MedicationOrderStatus;
import com.uco.ucopetapi.dto.medicationOrder.SignatureVerificationDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.exception.clinical.ClinicalException;
import com.uco.ucopetapi.repository.medicationOrder.MedicationOrderRepository;
import com.uco.ucopetapi.service.attention.CurrentDoctor;
import com.uco.ucopetapi.service.attention.CurrentDoctorProvider;
import com.uco.ucopetapi.service.attention.OpenPetCare;
import com.uco.ucopetapi.service.attention.OpenPetCareLoader;
import com.uco.ucopetapi.service.product.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class MedicationOrderService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter ORDER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter SIGNED_AT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final MedicationOrderRepository medicationOrderRepository;
    private final CurrentDoctorProvider currentDoctorProvider;
    private final OpenPetCareLoader openPetCareLoader;
    private final ProductService productService;

    public MedicationOrderService(final MedicationOrderRepository medicationOrderRepository,
                                  final CurrentDoctorProvider currentDoctorProvider,
                                  final OpenPetCareLoader openPetCareLoader,
                                  final ProductService productService) {
        this.medicationOrderRepository = medicationOrderRepository;
        this.currentDoctorProvider = currentDoctorProvider;
        this.openPetCareLoader = openPetCareLoader;
        this.productService = productService;
    }

    @Transactional
    public MedicationOrderDTO issue(final UUID petCareId, final CreateMedicationOrderRequest request) {
        CurrentDoctor doctor = currentDoctorProvider.get();
        OpenPetCare openPetCare = openPetCareLoader.load(petCareId);

        LocalDateTime signedAt = LocalDateTime.now(ZONE_ID).truncatedTo(ChronoUnit.SECONDS);
        MedicationOrderDomain order = new MedicationOrderDomain();
        order.setId(UUID.randomUUID());
        order.setOrderNumber(nextOrderNumber());
        order.setPetCareId(openPetCare.getPetCareId());
        order.setEpisodeId(openPetCare.getEpisodeId());
        order.setPetId(openPetCare.getPetId());
        order.setNotes(request == null ? null : request.getNotes());
        order.setStatus(MedicationOrderStatus.ISSUED);
        order.setSignedByDoctorId(doctor.getDoctorId());
        order.setSignedByPersonId(doctor.getPersonId());
        order.setSignedByName(doctor.getFullName());
        order.setSignedByLicense(doctor.getLicenseNumber());
        order.setSignedAt(signedAt);

        if (request != null && request.getItems() != null) {
            for (MedicationOrderItemRequest itemRequest : request.getItems()) {
                order.addItem(toItem(itemRequest));
            }
        }
        order.setSignatureHash(sha256(canonical(order)));
        order.validate();

        // TODO(equipo): dispensar con StockService.adjustStock y pasar a DISPENSED.
        return toDto(medicationOrderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public MedicationOrderDTO findById(final UUID id) {
        return toDto(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<MedicationOrderDTO> findByPetCare(final UUID petCareId) {
        openPetCareLoader.ensureExists(petCareId);
        return medicationOrderRepository.findByPetCareIdOrderBySignedAtDesc(petCareId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicationOrderDTO> findByPet(final UUID petId) {
        return medicationOrderRepository.findByPetIdOrderBySignedAtDesc(petId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public SignatureVerificationDTO verifySignature(final UUID id) {
        MedicationOrderDomain order = findEntity(id);
        String recalculated = sha256(canonical(order));
        boolean valid = order.getSignatureHash() != null && MessageDigest.isEqual(
                recalculated.getBytes(StandardCharsets.UTF_8),
                order.getSignatureHash().getBytes(StandardCharsets.UTF_8));
        return new SignatureVerificationDTO(order.getId(), valid);
    }

    @Transactional
    public MedicationOrderDTO cancel(final UUID id, final CancellationRequest request) {
        CurrentDoctor doctor = currentDoctorProvider.get();
        MedicationOrderDomain order = findEntity(id);
        order.cancel(doctor.getDoctorId(), request == null ? null : request.getReason(),
                LocalDateTime.now(ZONE_ID).truncatedTo(ChronoUnit.SECONDS));
        order.validate();
        return toDto(medicationOrderRepository.save(order));
    }

    private MedicationOrderItemDomain toItem(final MedicationOrderItemRequest request) {
        MedicationOrderItemDomain item = new MedicationOrderItemDomain();
        item.setId(UUID.randomUUID());
        item.setQuantity(request.getQuantity());
        item.setDose(request.getDose() == null ? null : request.getDose().trim());
        item.setFrequency(request.getFrequency() == null ? null : request.getFrequency().trim());
        item.setDurationDays(request.getDurationDays());
        item.setRoute(request.getRoute());
        item.setInstructions(request.getInstructions());
        if (request.getProductId() == null) {
            return item;
        }

        ProductDTO product = productService.getById(request.getProductId(), null);
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw ClinicalException.badRequest("El producto no está activo");
        }
        // TODO(equipo): validar categoría de medicamento cuando exista en ProductCategory.

        item.setProductId(product.getId());
        item.setProductName(product.getName());
        return item;
    }

    private MedicationOrderDomain findEntity(final UUID id) {
        return medicationOrderRepository.findWithItemsById(id)
                .orElseThrow(() -> ClinicalException.notFound("No se encontró la orden de medicamentos indicada"));
    }

    private String nextOrderNumber() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "RX-" + LocalDate.now(ZONE_ID).format(ORDER_DATE) + "-" + suffix;
    }

    private String canonical(final MedicationOrderDomain order) {
        StringBuilder builder = new StringBuilder();
        builder.append(text(order.getOrderNumber())).append('|')
                .append(text(order.getPetCareId())).append('|')
                .append(text(order.getPetId())).append('|')
                .append(text(order.getSignedByDoctorId())).append('|')
                .append(text(order.getSignedByName())).append('|')
                .append(text(order.getSignedByLicense())).append('|')
                .append(formatSignedAt(order.getSignedAt())).append('|')
                .append(text(order.getNotes()));

        List<MedicationOrderItemDomain> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getLineNumber() == null
                        ? Integer.MAX_VALUE
                        : item.getLineNumber()))
                .toList();
        for (MedicationOrderItemDomain item : items) {
            builder.append('|')
                    .append(text(item.getLineNumber())).append(';')
                    .append(text(item.getProductId())).append(';')
                    .append(text(item.getQuantity())).append(';')
                    .append(text(item.getDose())).append(';')
                    .append(text(item.getFrequency())).append(';')
                    .append(text(item.getDurationDays())).append(';')
                    .append(text(item.getRoute())).append(';')
                    .append(text(item.getInstructions()));
        }
        return builder.toString();
    }

    private String formatSignedAt(final LocalDateTime signedAt) {
        if (signedAt == null) {
            return "";
        }
        return signedAt.truncatedTo(ChronoUnit.SECONDS).format(SIGNED_AT_FORMAT);
    }

    private String text(final Object value) {
        return value == null ? "" : value.toString();
    }

    private String sha256(final String canonical) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no está disponible", ex);
        }
    }

    private MedicationOrderDTO toDto(final MedicationOrderDomain order) {
        List<MedicationOrderItemDTO> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream()
                .sorted(Comparator.comparing(item -> item.getLineNumber() == null
                        ? Integer.MAX_VALUE
                        : item.getLineNumber()))
                .map(this::toItemDto)
                .toList();
        return new MedicationOrderDTO(
                order.getId(),
                order.getOrderNumber(),
                order.getPetCareId(),
                order.getEpisodeId(),
                order.getPetId(),
                order.getNotes(),
                order.getStatus(),
                new MedicationOrderSignatureDTO(
                        order.getSignedByDoctorId(),
                        order.getSignedByName(),
                        order.getSignedByLicense(),
                        order.getSignedAt(),
                        order.getSignatureHash()
                ),
                order.getCancelledAt(),
                order.getCancellationReason(),
                items
        );
    }

    private MedicationOrderItemDTO toItemDto(final MedicationOrderItemDomain item) {
        return new MedicationOrderItemDTO(
                item.getId(),
                item.getLineNumber(),
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getDose(),
                item.getFrequency(),
                item.getDurationDays(),
                item.getRoute(),
                item.getInstructions()
        );
    }
}
