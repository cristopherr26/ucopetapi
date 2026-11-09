package com.uco.ucopetapi.service.transfer;

import com.uco.ucopetapi.domain.transfer.TransferDomain;
import com.uco.ucopetapi.dto.headquarter.HeadquarterDTO;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.product.StockDTO;
import com.uco.ucopetapi.dto.transfers.TransferCancelMessageDTO;
import com.uco.ucopetapi.dto.transfers.TransferRequestDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO.RelatedEntityDTO;
import com.uco.ucopetapi.dto.transfers.TransferSaveMessageDTO;
import com.uco.ucopetapi.dto.transfers.TransferStatus;
import com.uco.ucopetapi.dto.transfers.TransferStatusMessageDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.messaging.transfer.TransferCancelSenderBroker;
import com.uco.ucopetapi.messaging.transfer.TransferSaveSenderBroker;
import com.uco.ucopetapi.messaging.transfer.TransferStatusSenderBroker;
import com.uco.ucopetapi.repository.transfer.ITransferRepository;
import com.uco.ucopetapi.service.headquarter.HeadquarterService;
import com.uco.ucopetapi.service.person.PersonService;
import com.uco.ucopetapi.service.product.StockService;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferRequestException;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferStateException;
import com.uco.ucopetapi.service.transfer.exception.TransferNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class TransferService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");

    private final ITransferRepository transferRepository;
    private final HeadquarterService headquarterService;
    private final PersonService personService;
    private final StockService stockService;
    private final TransferSaveSenderBroker saveSender;
    private final TransferStatusSenderBroker statusSender;
    private final TransferCancelSenderBroker cancelSender;

    public TransferService(ITransferRepository transferRepository,
                           HeadquarterService headquarterService,
                           PersonService personService,
                           StockService stockService,
                           TransferSaveSenderBroker saveSender,
                           TransferStatusSenderBroker statusSender,
                           TransferCancelSenderBroker cancelSender) {
        this.transferRepository = transferRepository;
        this.headquarterService = headquarterService;
        this.personService = personService;
        this.stockService = stockService;
        this.saveSender = saveSender;
        this.statusSender = statusSender;
        this.cancelSender = cancelSender;
    }

    // ---------- Escrituras: validan y publican; el procesador guarda ----------

    public TransferResponseDTO createTransfer(TransferRequestDTO request, UUID performedBy) {
        validateDifferentHeadquarters(request.originHeadquarterId(), request.destinationHeadquarterId());
        HeadquarterDTO origin = getActiveHeadquarter(request.originHeadquarterId());
        getActiveHeadquarter(request.destinationHeadquarterId());
        PersonDTO creator = getExistingPerson(performedBy);
        validateStock(request.productId(), origin.getId(), request.quantity());

        TransferDomain transfer = new TransferDomain();
        transfer.setId(UUID.randomUUID());
        applyRequest(transfer, request);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setCreatedBy(creator.id());
        transfer.setCreatedAt(now());

        publishSave(transfer, creator.id());
        return toResponseDTO(transfer, new HashMap<>(), new HashMap<>());
    }

    public TransferResponseDTO updateTransfer(UUID id, TransferRequestDTO request, UUID performedBy) {
        TransferDomain existing = getExistingTransfer(id);
        requireStatus(existing, TransferStatus.PENDING, "editar");

        validateDifferentHeadquarters(request.originHeadquarterId(), request.destinationHeadquarterId());
        getActiveHeadquarter(request.originHeadquarterId());
        getActiveHeadquarter(request.destinationHeadquarterId());
        PersonDTO editor = getExistingPerson(performedBy);
        validateStock(request.productId(), request.originHeadquarterId(), request.quantity());

        TransferDomain transfer = copyOf(existing);
        applyRequest(transfer, request);
        transfer.setUpdatedBy(editor.id());
        transfer.setUpdatedAt(now());

        publishSave(transfer, editor.id());
        return toResponseDTO(transfer, new HashMap<>(), new HashMap<>());
    }

    public TransferResponseDTO updateTransferStatus(UUID id, TransferStatus newStatus, UUID performedBy) {
        TransferDomain existing = getExistingTransfer(id);
        requireOpen(existing, "actualizar el estado de");
        validateStatusTransition(existing.getStatus(), newStatus);
        PersonDTO actor = getExistingPerson(performedBy);

        statusSender.execute(new TransferStatusMessageDTO(id, newStatus, actor.id()), newMessageId());

        TransferDomain transfer = copyOf(existing);
        transfer.setStatus(newStatus);
        transfer.setUpdatedBy(actor.id());
        transfer.setUpdatedAt(now());
        return toResponseDTO(transfer, new HashMap<>(), new HashMap<>());
    }

    public void cancelTransfer(UUID id, UUID performedBy) {
        requireOpen(getExistingTransfer(id), "cancelar");
        PersonDTO actor = getExistingPerson(performedBy);

        cancelSender.execute(new TransferCancelMessageDTO(id, actor.id()), newMessageId());
    }

    // ---------- Lecturas ----------

    @Transactional(readOnly = true)
    public TransferResponseDTO findById(UUID id) {
        return toResponseDTO(getExistingTransfer(id), new HashMap<>(), new HashMap<>());
    }

    @Transactional(readOnly = true)
    public List<TransferResponseDTO> findByFilter(TransferStatus status, UUID originHeadquarterId,
                                                  UUID destinationHeadquarterId) {
        Map<UUID, String> headquarterNames = new HashMap<>();
        Map<UUID, String> personNames = new HashMap<>();
        return transferRepository.findByFilter(status, originHeadquarterId, destinationHeadquarterId)
                .stream()
                .map(transfer -> toResponseDTO(transfer, headquarterNames, personNames))
                .toList();
    }

    // ---------- Publicacion ----------

    private void publishSave(TransferDomain transfer, UUID performedBy) {
        saveSender.execute(new TransferSaveMessageDTO(transfer.getId(), transfer.getOriginHeadquarterId(),
                transfer.getDestinationHeadquarterId(), transfer.getProductId(), transfer.getQuantity(),
                TransferStatus.PENDING, transfer.getObservations(), performedBy), newMessageId());
    }

    private static String newMessageId() {
        return UUID.randomUUID().toString();
    }

    private static LocalDateTime now() {
        return LocalDateTime.now(ZONE_ID);
    }

    // ---------- Busquedas y validaciones ----------

    private TransferDomain getExistingTransfer(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException("No existe un traslado con id " + id));
    }

    private PersonDTO getExistingPerson(UUID personId) {
        try {
            return personService.findById(personId);
        } catch (ResponseStatusException _) {
            throw new InvalidTransferRequestException("No existe un usuario con id " + personId);
        }
    }

    private HeadquarterDTO getActiveHeadquarter(UUID headquarterId) {
        HeadquarterDTO headquarter;
        try {
            headquarter = headquarterService.findById(headquarterId);
        } catch (BusinessException _) {
            throw new InvalidTransferRequestException("No existe una sede con id " + headquarterId);
        }
        if (!Boolean.TRUE.equals(headquarter.getIsActive())) {
            throw new InvalidTransferRequestException(
                    "La sede '" + headquarter.getName() + "' no esta activa, no se puede usar en un traslado");
        }
        return headquarter;
    }

    private void requireStatus(TransferDomain transfer, TransferStatus required, String accion) {
        if (transfer.getStatus() != required) {
            throw new InvalidTransferStateException(
                    "Solo se puede " + accion + " un traslado en estado " + required
                            + ". Estado actual: " + transfer.getStatus());
        }
    }

    private void requireOpen(TransferDomain transfer, String accion) {
        if (transfer.getStatus() != TransferStatus.PENDING && transfer.getStatus() != TransferStatus.IN_PROGRESS) {
            throw new InvalidTransferStateException(
                    "Solo se puede " + accion + " un traslado Pendiente o En Proceso. Estado actual: "
                            + transfer.getStatus());
        }
    }

    private void validateDifferentHeadquarters(UUID originId, UUID destinationId) {
        if (originId != null && originId.equals(destinationId)) {
            throw new InvalidTransferRequestException("La sede de origen y la sede de destino no pueden ser la misma");
        }
    }

    private void validateStatusTransition(TransferStatus current, TransferStatus next) {
        boolean valid = (current == TransferStatus.PENDING && next == TransferStatus.IN_PROGRESS)
                || (current == TransferStatus.IN_PROGRESS && next == TransferStatus.COMPLETED);
        if (!valid) {
            throw new InvalidTransferStateException(
                    "Transicion de estado invalida: no se puede pasar de " + current + " a " + next);
        }
    }

    private void validateStock(UUID productId, UUID originHeadquarterId, Integer quantity) {
        StockDTO stock;
        try {
            stock = stockService.findByProductAndHeadquarter(productId, originHeadquarterId);
        } catch (NoSuchElementException ex) {
            throw new InvalidTransferRequestException(ex.getMessage());
        }
        int available = stock.getQuantity() == null ? 0 : stock.getQuantity();
        if (available < quantity) {
            throw new InvalidTransferRequestException(
                    "No hay suficiente stock del producto en la sede de origen. Disponible: "
                            + available + ", solicitado: " + quantity);
        }
    }

    // ---------- Mapeo ----------

    private void applyRequest(TransferDomain transfer, TransferRequestDTO request) {
        transfer.setOriginHeadquarterId(request.originHeadquarterId());
        transfer.setDestinationHeadquarterId(request.destinationHeadquarterId());
        transfer.setProductId(request.productId());
        transfer.setQuantity(request.quantity());
        transfer.setObservations(request.observations());
    }

    // Copia suelta: la entidad cargada no se toca, solo se usa para armar la respuesta.
    private TransferDomain copyOf(TransferDomain source) {
        TransferDomain copy = new TransferDomain();
        copy.setId(source.getId());
        copy.setOriginHeadquarterId(source.getOriginHeadquarterId());
        copy.setDestinationHeadquarterId(source.getDestinationHeadquarterId());
        copy.setProductId(source.getProductId());
        copy.setQuantity(source.getQuantity());
        copy.setStatus(source.getStatus());
        copy.setObservations(source.getObservations());
        copy.setCreatedBy(source.getCreatedBy());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setUpdatedBy(source.getUpdatedBy());
        copy.setUpdatedAt(source.getUpdatedAt());
        return copy;
    }

    private TransferResponseDTO toResponseDTO(TransferDomain transfer,
                                              Map<UUID, String> headquarterNames,
                                              Map<UUID, String> personNames) {
        return new TransferResponseDTO(
                transfer.getId(),
                headquarterRef(transfer.getOriginHeadquarterId(), headquarterNames),
                headquarterRef(transfer.getDestinationHeadquarterId(), headquarterNames),
                new RelatedEntityDTO(transfer.getProductId(), null),
                transfer.getQuantity(),
                transfer.getStatus(),
                transfer.getObservations(),
                personRef(transfer.getCreatedBy(), personNames),
                transfer.getCreatedAt(),
                transfer.getUpdatedBy() != null ? personRef(transfer.getUpdatedBy(), personNames) : null,
                transfer.getUpdatedAt());
    }

    private RelatedEntityDTO headquarterRef(UUID id, Map<UUID, String> cache) {
        String name = cache.computeIfAbsent(id, key -> {
            try {
                return headquarterService.findById(key).getName();
            } catch (BusinessException _) {
                return null;
            }
        });
        return new RelatedEntityDTO(id, name);
    }

    private RelatedEntityDTO personRef(UUID id, Map<UUID, String> cache) {
        String name = cache.computeIfAbsent(id, key -> {
            try {
                PersonDTO person = personService.findById(key);
                return person.firstName() + " " + person.lastName();
            } catch (ResponseStatusException _) {
                return null;
            }
        });
        return new RelatedEntityDTO(id, name);
    }
}