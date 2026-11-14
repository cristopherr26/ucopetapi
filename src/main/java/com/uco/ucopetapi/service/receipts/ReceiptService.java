package com.uco.ucopetapi.service.receipts;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
import com.uco.ucopetapi.domain.receipts.ReceiptDomain;
import com.uco.ucopetapi.dto.receipts.ReceiptAcceptedDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptRequestDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptResponseDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptStatus;
import com.uco.ucopetapi.messaging.receipts.ReceiptCancelSenderBroker;
import com.uco.ucopetapi.messaging.receipts.ReceiptCreationSenderBroker;
import com.uco.ucopetapi.messaging.receipts.ReceiptUpdateSenderBroker;
import com.uco.ucopetapi.repository.payMethod.PayMethodRepository;
import com.uco.ucopetapi.repository.receipts.ReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class ReceiptService {

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    private final ReceiptRepository receiptRepository;
    private final PayMethodRepository payMethodRepository;
    private final ReceiptCreationSenderBroker creationSender;
    private final ReceiptUpdateSenderBroker updateSender;
    private final ReceiptCancelSenderBroker cancelSender;

    public ReceiptService(final ReceiptRepository receiptRepository,
                          final PayMethodRepository payMethodRepository,
                          final ReceiptCreationSenderBroker creationSender,
                          final ReceiptUpdateSenderBroker updateSender,
                          final ReceiptCancelSenderBroker cancelSender) {
        this.receiptRepository = receiptRepository;
        this.payMethodRepository = payMethodRepository;
        this.creationSender = creationSender;
        this.updateSender = updateSender;
        this.cancelSender = cancelSender;
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponseDTO> findAll() {
        return receiptRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReceiptResponseDTO findById(final UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponseDTO> findByFilter(final UUID tutorId, final ReceiptStatus state) {
        final List<ReceiptDomain> receipts;
        if (tutorId != null && state != null) {
            receipts = receiptRepository.findByTutorIdAndState(tutorId, state);
        } else if (tutorId != null) {
            receipts = receiptRepository.findByTutorId(tutorId);
        } else if (state != null) {
            receipts = receiptRepository.findByState(state);
        } else {
            receipts = receiptRepository.findAll();
        }
        return receipts.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal dailyTotal(final LocalDateTime startDate, final LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new ReceiptValidationException("Las fechas de inicio y fin son obligatorias");
        }
        if (startDate.isAfter(endDate)) {
            throw new ReceiptValidationException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return receiptRepository.sumAmountByStateAndDateBetween(ReceiptStatus.ACTIVE, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public ReceiptAcceptedDTO create(final ReceiptRequestDTO request) {
        validateRequest(request);
        final PayMethodDomain payMethod = resolvePayMethod(request.payMethodId());
        final LocalDateTime date = resolveDate(request.date());

        // Instancia nueva: la API no la guarda, solo viaja en el mensaje.
        // El número y el estado definitivos los asigna el procesador.
        final ReceiptDomain receipt = ReceiptDomain.builder()
                .id(UUID.randomUUID())
                .tutorId(request.tutorId())
                .petId(request.petId())
                .concept(request.concept().trim())
                .amount(request.amount())
                .payMethod(detached(payMethod))
                .date(date)
                .state(ReceiptStatus.ACTIVE)
                .build();

        final String idMessage = UUID.randomUUID().toString();
        creationSender.execute(receipt, idMessage);
        return new ReceiptAcceptedDTO(receipt.getId(), idMessage);
    }

    @Transactional(readOnly = true)
    public ReceiptAcceptedDTO update(final UUID id, final ReceiptRequestDTO request) {
        final ReceiptDomain current = getOrThrow(id);
        if (current.getState() == ReceiptStatus.CANCELLED) {
            throw new ReceiptStateConflictException("No se puede modificar un recibo anulado");
        }

        validateRequest(request);
        final PayMethodDomain payMethod = resolvePayMethod(request.payMethodId());
        final LocalDateTime date = request.date() != null ? resolveDate(request.date()) : current.getDate();

        // Instancia nueva con el estado final; la entidad cargada no se modifica.
        final ReceiptDomain receipt = ReceiptDomain.builder()
                .id(id)
                .tutorId(request.tutorId())
                .petId(request.petId())
                .concept(request.concept().trim())
                .amount(request.amount())
                .payMethod(detached(payMethod))
                .date(date)
                .state(ReceiptStatus.ACTIVE)
                .build();

        final String idMessage = UUID.randomUUID().toString();
        updateSender.execute(receipt, idMessage);
        return new ReceiptAcceptedDTO(id, idMessage);
    }

    @Transactional(readOnly = true)
    public ReceiptAcceptedDTO cancel(final UUID id) {
        final ReceiptDomain current = getOrThrow(id);
        if (current.getState() == ReceiptStatus.CANCELLED) {
            throw new ReceiptStateConflictException("El recibo ya está anulado");
        }

        final ReceiptDomain receipt = ReceiptDomain.builder()
                .id(id)
                .state(ReceiptStatus.CANCELLED)
                .build();

        final String idMessage = UUID.randomUUID().toString();
        cancelSender.execute(receipt, idMessage);
        return new ReceiptAcceptedDTO(id, idMessage);
    }

    private void validateRequest(final ReceiptRequestDTO request) {
        if (request.tutorId() == null) {
            throw new ReceiptValidationException("El tutor es obligatorio");
        }
        if (request.concept() == null || request.concept().isBlank()) {
            throw new ReceiptValidationException("El concepto del recibo es obligatorio");
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReceiptValidationException("El valor del recibo debe ser mayor a cero");
        }
    }

    private LocalDateTime resolveDate(final LocalDateTime requested) {
        final LocalDateTime now = LocalDateTime.now(ZONE);
        if (requested == null) {
            return now;
        }
        if (requested.isAfter(now)) {
            throw new ReceiptValidationException("La fecha del recibo no puede ser futura");
        }
        return requested;
    }

    private PayMethodDomain resolvePayMethod(final UUID payMethodId) {
        if (payMethodId == null) {
            throw new ReceiptValidationException("El método de pago es obligatorio");
        }
        return payMethodRepository.findById(payMethodId)
                .orElseThrow(() -> new ReceiptValidationException("El método de pago no existe"));
    }

    // Copia suelta (sin proxy de Hibernate) para poder serializarla en el mensaje.
    private PayMethodDomain detached(final PayMethodDomain payMethod) {
        return new PayMethodDomain(payMethod.getId(), payMethod.getName());
    }

    private ReceiptDomain getOrThrow(final UUID id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException(id));
    }

    private ReceiptResponseDTO toResponse(final ReceiptDomain receipt) {
        return new ReceiptResponseDTO(
                receipt.getId(),
                receipt.getReceiptNumber(),
                receipt.getTutorId(),
                receipt.getPetId(),
                receipt.getConcept(),
                receipt.getAmount(),
                receipt.getPayMethod().getId(),
                receipt.getPayMethod().getName(),
                receipt.getDate(),
                receipt.getState()
        );
    }
}