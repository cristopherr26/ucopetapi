package com.uco.ucopetapi.service.transfer;

import com.uco.ucopetapi.domain.headquarter.HeadquarterDomain;
import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.domain.transfer.TransferDomain;
import com.uco.ucopetapi.dto.transfers.TransferRequestDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO;
import com.uco.ucopetapi.dto.transfers.TransferResponseDTO.RelatedEntityDTO;
import com.uco.ucopetapi.dto.transfers.TransferStatus;
import com.uco.ucopetapi.repository.headquarter.HeadquarterRepository;
import com.uco.ucopetapi.repository.person.PersonRepository;
import com.uco.ucopetapi.repository.transfer.ITransferRepository;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferRequestException;
import com.uco.ucopetapi.service.transfer.exception.InvalidTransferStateException;
import com.uco.ucopetapi.service.transfer.exception.TransferNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * TODO integracion pendiente: cuando el modulo de Product exponga un metodo para
 * consultar stock disponible por sede, agregar la validacion en validateStock().
 */
@Service
public class TransferService {

    private final ITransferRepository transferRepository;
    private final HeadquarterRepository headquarterRepository;
    private final PersonRepository personRepository;

    public TransferService(ITransferRepository transferRepository,
                           HeadquarterRepository headquarterRepository,
                           PersonRepository personRepository) {
        this.transferRepository = transferRepository;
        this.headquarterRepository = headquarterRepository;
        this.personRepository = personRepository;
    }

    public TransferResponseDTO createTransfer(TransferRequestDTO request, UUID performedBy) {
        validateDifferentHeadquarters(request.originHeadquarterId(), request.destinationHeadquarterId());
        HeadquarterDomain origin = getActiveHeadquarter(request.originHeadquarterId());
        HeadquarterDomain destination = getActiveHeadquarter(request.destinationHeadquarterId());
        PersonDomain creator = getExistingPerson(performedBy);
        validateStock(request.productId(), origin.getId(), request.quantity());

        TransferDomain transfer = new TransferDomain();
        transfer.setOriginHeadquarter(origin);
        transfer.setDestinationHeadquarter(destination);
        transfer.setProductId(request.productId());
        transfer.setQuantity(request.quantity());
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setObservations(request.observations());
        transfer.setCreatedBy(creator);
        transfer.setCreatedAt(LocalDateTime.now());

        return toResponseDTO(transferRepository.save(transfer));
    }

    public TransferResponseDTO updateTransfer(UUID id, TransferRequestDTO request) {
        TransferDomain transfer = getExistingTransfer(id);
        requireStatus(transfer, TransferStatus.PENDING, "editar");

        validateDifferentHeadquarters(request.originHeadquarterId(), request.destinationHeadquarterId());
        HeadquarterDomain origin = getActiveHeadquarter(request.originHeadquarterId());
        HeadquarterDomain destination = getActiveHeadquarter(request.destinationHeadquarterId());
        validateStock(request.productId(), origin.getId(), request.quantity());

        transfer.setOriginHeadquarter(origin);
        transfer.setDestinationHeadquarter(destination);
        transfer.setProductId(request.productId());
        transfer.setQuantity(request.quantity());
        transfer.setObservations(request.observations());

        return toResponseDTO(transferRepository.save(transfer));
    }

    public TransferResponseDTO updateTransferStatus(UUID id, TransferStatus newStatus, UUID performedBy) {
        TransferDomain transfer = getExistingTransfer(id);

        if (transfer.getStatus() != TransferStatus.PENDING && transfer.getStatus() != TransferStatus.IN_PROGRESS) {
            throw new InvalidTransferStateException(
                    "Solo se puede actualizar el estado de un traslado Pendiente o En Proceso. Estado actual: "
                            + transfer.getStatus());
        }
        validateStatusTransition(transfer.getStatus(), newStatus);

        transfer.setStatus(newStatus);
        transfer.setUpdatedBy(getExistingPerson(performedBy));
        transfer.setUpdatedAt(LocalDateTime.now());

        return toResponseDTO(transferRepository.save(transfer));
    }

    public void cancelTransfer(UUID id, UUID performedBy) {
        TransferDomain transfer = getExistingTransfer(id);
        requireStatus(transfer, TransferStatus.PENDING, "cancelar");

        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setUpdatedBy(getExistingPerson(performedBy));
        transfer.setUpdatedAt(LocalDateTime.now());

        transferRepository.save(transfer);
    }

    public TransferResponseDTO findById(UUID id) {
        return toResponseDTO(getExistingTransfer(id));
    }

    public List<TransferResponseDTO> findByFilter(TransferStatus status, UUID originHeadquarterId, UUID destinationHeadquarterId) {
        return transferRepository.findByFilter(status, originHeadquarterId, destinationHeadquarterId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private TransferDomain getExistingTransfer(UUID id) {
        return transferRepository.findById(id)
                .orElseThrow(() -> new TransferNotFoundException("No existe un traslado con id " + id));
    }

    private PersonDomain getExistingPerson(UUID personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new InvalidTransferRequestException("No existe un usuario con id " + personId));
    }

    private void requireStatus(TransferDomain transfer, TransferStatus required, String accion) {
        if (transfer.getStatus() != required) {
            throw new InvalidTransferStateException(
                    "Solo se puede " + accion + " un traslado en estado " + required
                            + ". Estado actual: " + transfer.getStatus());
        }
    }

    private void validateDifferentHeadquarters(UUID originId, UUID destinationId) {
        if (originId != null && originId.equals(destinationId)) {
            throw new InvalidTransferRequestException("La sede de origen y la sede de destino no pueden ser la misma");
        }
    }

    private HeadquarterDomain getActiveHeadquarter(UUID headquarterId) {
        HeadquarterDomain headquarter = headquarterRepository.findById(headquarterId)
                .orElseThrow(() -> new InvalidTransferRequestException("No existe una sede con id " + headquarterId));

        if (!Boolean.TRUE.equals(headquarter.getIsActive())) {
            throw new InvalidTransferRequestException(
                    "La sede '" + headquarter.getName() + "' no esta activa, no se puede usar en un traslado");
        }
        return headquarter;
    }

    private void validateStatusTransition(TransferStatus current, TransferStatus next) {
        boolean isValidTransition =
                (current == TransferStatus.PENDING && next == TransferStatus.IN_PROGRESS)
                        || (current == TransferStatus.IN_PROGRESS && next == TransferStatus.COMPLETED);

        if (!isValidTransition) {
            throw new InvalidTransferStateException(
                    "Transicion de estado invalida: no se puede pasar de " + current + " a " + next);
        }
    }

    /**
     * TODO: conectar con el metodo de stock que va a exponer el modulo de Product.
     */
    private void validateStock(UUID productId, UUID originHeadquarterId, Integer quantity) {
        // Placeholder intencional.
    }

    private TransferResponseDTO toResponseDTO(TransferDomain transfer) {
        return new TransferResponseDTO(
                transfer.getId(),
                new RelatedEntityDTO(transfer.getOriginHeadquarter().getId(), transfer.getOriginHeadquarter().getName()),
                new RelatedEntityDTO(transfer.getDestinationHeadquarter().getId(), transfer.getDestinationHeadquarter().getName()),
                new RelatedEntityDTO(transfer.getProductId(), null),
                transfer.getQuantity(),
                transfer.getStatus(),
                transfer.getObservations(),
                toPersonDTO(transfer.getCreatedBy()),
                transfer.getCreatedAt(),
                transfer.getUpdatedBy() != null ? toPersonDTO(transfer.getUpdatedBy()) : null,
                transfer.getUpdatedAt()
        );
    }

    private RelatedEntityDTO toPersonDTO(PersonDomain person) {
        return new RelatedEntityDTO(person.getId(), person.getFirstName() + " " + person.getLastName());
    }
}
