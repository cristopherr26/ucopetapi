package com.uco.ucopetapi.service.receipts;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
import com.uco.ucopetapi.domain.receipts.ReceiptDomain;
import com.uco.ucopetapi.dto.receipts.ReceiptRequestDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptResponseDTO;
import com.uco.ucopetapi.dto.receipts.ReceiptStatus;
import com.uco.ucopetapi.repository.payMethod.PayMethodRepository;
import com.uco.ucopetapi.repository.receipts.ReceiptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final PayMethodRepository payMethodRepository;

    public ReceiptService(final ReceiptRepository receiptRepository,
                          final PayMethodRepository payMethodRepository) {
        this.receiptRepository = receiptRepository;
        this.payMethodRepository = payMethodRepository;
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponseDTO> findAll() {
        return receiptRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
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

    @Transactional
    public ReceiptResponseDTO create(final ReceiptRequestDTO request) {
        validateAmount(request.amount());
        validateConcept(request.concept());
        if (request.tutorId() == null) {
            throw new ReceiptValidationException("El tutor es obligatorio");
        }

        final PayMethodDomain payMethod = resolvePayMethod(request.payMethodId());
        final LocalDateTime date = request.date() != null ? request.date() : LocalDateTime.now();
        validateDateNotInFuture(date);

        final ReceiptDomain receipt = new ReceiptDomain(
                UUID.randomUUID(),
                nextReceiptNumber(),
                request.tutorId(),
                request.petId(),
                request.concept(),
                request.amount(),
                payMethod,
                date,
                ReceiptStatus.ACTIVE
        );

        return toResponse(receiptRepository.save(receipt));
    }

    @Transactional
    public ReceiptResponseDTO update(final UUID id, final ReceiptRequestDTO request) {
        final ReceiptDomain receipt = getOrThrow(id);

        if (receipt.getState() == ReceiptStatus.CANCELLED) {
            throw new ReceiptValidationException("No se puede modificar un recibo anulado: " + id);
        }

        if (request.tutorId() != null) {
            receipt.setTutorId(request.tutorId());
        }

        receipt.setPetId(request.petId());

        if (request.concept() != null) {
            validateConcept(request.concept());
            receipt.setConcept(request.concept());
        }
        if (request.amount() != null) {
            validateAmount(request.amount());
            receipt.setAmount(request.amount());
        }
        if (request.payMethodId() != null) {
            receipt.setPayMethod(resolvePayMethod(request.payMethodId()));
        }
        if (request.date() != null) {
            validateDateNotInFuture(request.date());
            receipt.setDate(request.date());
        }

        return toResponse(receiptRepository.save(receipt));
    }

    @Transactional
    public ReceiptResponseDTO cancel(final UUID id) {
        final ReceiptDomain receipt = getOrThrow(id);

        if (receipt.getState() == ReceiptStatus.CANCELLED) {
            throw new ReceiptValidationException("El recibo ya está anulado: " + id);
        }

        receipt.setState(ReceiptStatus.CANCELLED);
        return toResponse(receiptRepository.save(receipt));
    }

    public void delete(final UUID id) {
        getOrThrow(id);
        throw new ReceiptValidationException(
                "Los recibos de caja no se eliminan, se anulan. Usa cancelReceipt(" + id + ") en su lugar.");
    }

    private ReceiptDomain getOrThrow(final UUID id) {
        return receiptRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Recibo no encontrado: " + id));
    }

    private PayMethodDomain resolvePayMethod(final UUID payMethodId) {
        return payMethodRepository.findById(payMethodId)
                .orElseThrow(() -> new ReceiptValidationException("El método de pago no existe: " + payMethodId));
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
        if (date.isAfter(LocalDateTime.now())) {
            throw new ReceiptValidationException("La fecha del recibo no puede ser futura");
        }
    }

    private String nextReceiptNumber() {
        final long nextNumber = receiptRepository.count() + 1;
        return String.format("REC-%06d", nextNumber);
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
