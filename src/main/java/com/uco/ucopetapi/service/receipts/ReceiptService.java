package com.uco.ucopetapi.service.receipts;

import com.uco.ucopetapi.domain.receipts.ReceiptDomain;
import com.uco.ucopetapi.dto.receipts.ReceiptAcceptedDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptCancelMessageDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptMessageDTO;
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
        List<ReceiptDomain> receipts;
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

    public ReceiptAcceptedDTO create(final ReceiptRequestDTO request) {
        validateAmount(request.amount());
        validateConcept(request.concept());
        if (request.tutorId() == null) {
            throw new ReceiptValidationException("El tutor es obligatorio");
        }
        validatePayMethodExists(request.payMethodId());

        final LocalDateTime date = request.date() != null ? request.date() : LocalDateTime.now(ZONE);
        validateDateNotInFuture(date);

        final UUID receiptId = UUID.randomUUID();
        final String idMessage = UUID.randomUUID().toString();

        creationSender.execute(new ReceiptMessageDTO(
                receiptId,
                request.tutorId(),
                request.petId(),
                request.concept(),
                request.amount(),
                request.payMethodId(),
                date
        ), idMessage);

        return new ReceiptAcceptedDTO(receiptId, idMessage);
    }

    @Transactional(readOnly = true)
    public ReceiptAcceptedDTO update(final UUID id, final ReceiptRequestDTO request) {
        final ReceiptDomain receipt = getOrThrow(id);

        if (receipt.getState() == ReceiptStatus.CANCELLED) {
            throw new ReceiptValidationException("No se puede modificar un recibo anulado: " + id);
        }

        final UUID tutorId = request.tutorId() != null ? request.tutorId() : receipt.getTutorId();

        String concept = receipt.getConcept();
        if (request.concept() != null) {
            validateConcept(request.concept());
            concept = request.concept();
        }

        BigDecimal amount = receipt.getAmount();
        if (request.amount() != null) {
            validateAmount(request.amount());
            amount = request.amount();
        }

        UUID payMethodId = receipt.getPayMethod().getId();
        if (request.payMethodId() != null) {
            validatePayMethodExists(request.payMethodId());
            payMethodId = request.payMethodId();
        }

        LocalDateTime date = receipt.getDate();
        if (request.date() != null) {
            validateDateNotInFuture(request.date());
            date = request.date();
        }

        final String idMessage = UUID.randomUUID().toString();
        updateSender.execute(new ReceiptMessageDTO(
                id, tutorId, request.petId(), concept, amount, payMethodId, date
        ), idMessage);

        return new ReceiptAcceptedDTO(id, idMessage);
    }

    @Transactional(readOnly = true)
    public ReceiptAcceptedDTO cancel(final UUID id) {
        final ReceiptDomain receipt = getOrThrow(id);

        if (receipt.getState() == ReceiptStatus.CANCELLED) {
            throw new ReceiptValidationException("El recibo ya está anulado: " + id);
        }

        final String idMessage = UUID.randomUUID().toString();
        cancelSender.execute(new ReceiptCancelMessageDTO(id), idMessage);
        return new ReceiptAcceptedDTO(id, idMessage);
    }

    public void delete(final UUID id) {
        getOrThrow(id);
        throw new ReceiptValidationException(
                "Los recibos de caja no se eliminan, se anulan. Usa cancelReceipt(" + id + ") en su lugar.");
    }

    private ReceiptDomain getOrThrow(final UUID id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new ReceiptNotFoundException(id));
    }

    private void validatePayMethodExists(final UUID payMethodId) {
        if (payMethodId == null || !payMethodRepository.existsById(payMethodId)) {
            throw new ReceiptValidationException("El método de pago no existe: " + payMethodId);
        }
    }

    private void validateAmount(final BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReceiptValidationException("El valor del recibo debe ser mayor a cero");
        }
    }

    private void validateConcept(final String concept) {
        if (concept == null || concept.isBlank()) {
            throw new ReceiptValidationException("El concepto del recibo es obligatorio");
        }
    }

    private void validateDateNotInFuture(final LocalDateTime date) {
        if (date.isAfter(LocalDateTime.now(ZONE))) {
            throw new ReceiptValidationException("La fecha del recibo no puede ser futura");
        }
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