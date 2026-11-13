package com.uco.ucopetapi.service.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import com.uco.ucopetapi.domain.purchases.Purchase;
import com.uco.ucopetapi.domain.purchases.PurchaseStatus;
import com.uco.ucopetapi.dto.egress.EgressDTO;
import com.uco.ucopetapi.messaging.egress.EgressDeleteSenderBroker;
import com.uco.ucopetapi.messaging.egress.EgressSaveSenderBroker;
import com.uco.ucopetapi.repository.egress.EgressRepository;
import com.uco.ucopetapi.repository.payMethod.PayMethodRepository;
import com.uco.ucopetapi.repository.provider.ProviderJPARepository;
import com.uco.ucopetapi.repository.purchases.PurchaseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EgressService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");

    private final EgressRepository egressRepository;
    private final ProviderJPARepository providerRepository;
    private final PayMethodRepository payMethodRepository;
    private final PurchaseRepository purchaseRepository;
    private final EgressSaveSenderBroker egressSaveSender;
    private final EgressDeleteSenderBroker egressDeleteSender;

    public EgressService(EgressRepository egressRepository,
                         ProviderJPARepository providerRepository,
                         PayMethodRepository payMethodRepository,
                         PurchaseRepository purchaseRepository,
                         EgressSaveSenderBroker egressSaveSender,
                         EgressDeleteSenderBroker egressDeleteSender) {
        this.egressRepository = egressRepository;
        this.providerRepository = providerRepository;
        this.payMethodRepository = payMethodRepository;
        this.purchaseRepository = purchaseRepository;
        this.egressSaveSender = egressSaveSender;
        this.egressDeleteSender = egressDeleteSender;
    }

    public List<EgressDTO> getAll() {
        return toDTOs(egressRepository.findAll());
    }

    public EgressDTO getById(UUID id) {
        return toDTO(getEntityById(id));
    }

    public List<EgressDTO> getByConcept(String concept) {
        validateConcept(concept);
        return toDTOs(egressRepository.findByConcept(concept));
    }

    public List<EgressDTO> getByDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return toDTOs(egressRepository.findByDateBetween(startDate, endDate));
    }

    public List<EgressDTO> getByProvider(UUID providerId) {
        validateRequired(providerId, "El proveedor es obligatorio");
        return toDTOs(egressRepository.findByProvider(providerId));
    }

    public List<EgressDTO> getByPayMethod(UUID payMethodId) {
        validateRequired(payMethodId, "El método de pago es obligatorio");
        return toDTOs(egressRepository.findByPayMethod(payMethodId));
    }

    public List<EgressDTO> getByPurchaseOrder(UUID purchaseOrderId) {
        validateRequired(purchaseOrderId, "La orden de compra es obligatoria");
        return toDTOs(egressRepository.findByPurchaseOrder(purchaseOrderId));
    }

    public EgressDTO saveEgress(EgressDomain egress) {
        egress.setId(UUID.randomUUID());
        validateEgress(egress);

        EgressDTO message = toDTO(egress);
        egressSaveSender.execute(message, newMessageId());
        return message;
    }

    public EgressDTO updateEgress(UUID id, EgressDomain updatedEgress) {
        validateEgressExists(id);
        updatedEgress.setId(id);
        validateEgress(updatedEgress);

        EgressDTO message = toDTO(updatedEgress);
        egressSaveSender.execute(message, newMessageId());
        return message;
    }

    public void deleteEgress(UUID id) {
        validateEgressExists(id);
        egressDeleteSender.execute(id, newMessageId());
    }

    private void validateEgress(EgressDomain egress) {
        validateDate(egress.getDate());
        validateConcept(egress.getConcept());
        validateTotal(egress.getTotal());
        validateProvider(egress.getProvider());
        validatePayMethod(egress.getPayMethod());
        validatePurchaseOrder(egress);
        egress.setConcept(egress.getConcept().trim());
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (date.isAfter(LocalDate.now(ZONE_ID))) {
            throw new IllegalArgumentException("La fecha no puede ser posterior a la fecha actual");
        }
    }

    private void validateConcept(String concept) {
        if (concept == null || concept.isBlank()) {
            throw new IllegalArgumentException("El concepto es obligatorio");
        }
    }

    private void validateTotal(Float total) {
        if (total == null) {
            throw new IllegalArgumentException("El total es obligatorio");
        }
        if (total <= 0) {
            throw new IllegalArgumentException("El total no puede ser negativo ni cero");
        }
    }

    private void validateProvider(UUID providerId) {
        validateRequired(providerId, "El proveedor es obligatorio");
        if (!providerRepository.existsById(providerId)) {
            throw new IllegalArgumentException("No existe un proveedor con id: " + providerId);
        }
    }

    private void validatePayMethod(UUID payMethodId) {
        validateRequired(payMethodId, "El método de pago es obligatorio");
        if (!payMethodRepository.existsById(payMethodId)) {
            throw new IllegalArgumentException("No existe un método de pago con id: " + payMethodId);
        }
    }

    private void validatePurchaseOrder(EgressDomain egress) {
        UUID purchaseId = egress.getPurchaseOrder();
        validateRequired(purchaseId, "La orden de compra es obligatoria");

        Purchase purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new IllegalArgumentException("No existe una orden de compra con id: " + purchaseId));

        if (purchase.getStatus() == PurchaseStatus.CANCELLED) {
            throw new IllegalArgumentException("La orden de compra " + purchase.getPurchaseNumber() + " está cancelada");
        }
        if (!Objects.equals(purchase.getSupplierId(), egress.getProvider())) {
            throw new IllegalArgumentException("La orden de compra " + purchase.getPurchaseNumber()
                    + " no pertenece al proveedor seleccionado");
        }

        BigDecimal alreadyPaid = egressRepository.findByPurchaseOrder(purchaseId).stream()
                .filter(other -> !other.getId().equals(egress.getId()))
                .map(other -> toBigDecimal(other.getTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pending = purchase.getTotal().subtract(alreadyPaid);

        if (toBigDecimal(egress.getTotal()).compareTo(pending) > 0) {
            throw new IllegalArgumentException("El total supera el saldo pendiente de la orden "
                    + purchase.getPurchaseNumber() + " (" + pending.toPlainString() + ")");
        }
    }

    /** Float.toString evita arrastrar el error binario del float (0.1f -> 0.100000001...). */
    private BigDecimal toBigDecimal(Float value) {
        return new BigDecimal(Float.toString(value));
    }

    private void validateEgressExists(UUID id) {
        validateRequired(id, "El id del egreso es obligatorio");
        if (!egressRepository.existsById(id)) {
            throw new NoSuchElementException("Egreso no encontrado con id: " + id);
        }
    }

    private void validateRequired(UUID value, String errorMessage) {
        if (value == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private EgressDomain getEntityById(UUID id) {
        return egressRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Egreso no encontrado con id: " + id));
    }

    private List<EgressDTO> toDTOs(List<EgressDomain> egresses) {
        return egresses.stream().map(this::toDTO).toList();
    }

    private EgressDTO toDTO(EgressDomain egress) {
        return new EgressDTO(
                egress.getId(),
                egress.getDate(),
                egress.getProvider(),
                egress.getPayMethod(),
                egress.getPurchaseOrder(),
                egress.getConcept(),
                egress.getTotal()
        );
    }

    private String newMessageId() {
        return UUID.randomUUID().toString();
    }
}