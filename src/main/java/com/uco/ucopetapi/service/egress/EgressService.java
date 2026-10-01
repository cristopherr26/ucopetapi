package com.uco.ucopetapi.service.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import com.uco.ucopetapi.dto.egress.EgressDTO;
import com.uco.ucopetapi.repository.egress.EgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class EgressService {

    private final EgressRepository egressRepository;

    public EgressService(EgressRepository egressRepository) {
        this.egressRepository = egressRepository;
    }

    public List<EgressDTO> getAll() {
        return egressRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public EgressDTO getById(UUID id) {
        return toDTO(getEntityById(id));
    }

    public EgressDTO saveEgress(EgressDomain egress) {
        validateEgress(egress);
        return toDTO(egressRepository.save(egress));
    }

    public EgressDTO updateEgress(UUID id, EgressDomain updatedEgress) {
        EgressDomain existentEgress = getEntityById(id);
        validateEgress(updatedEgress);
        existentEgress.setDate(updatedEgress.getDate());
        existentEgress.setProvider(updatedEgress.getProvider());
        existentEgress.setPayMethod(updatedEgress.getPayMethod());
        existentEgress.setPurchaseOrder(updatedEgress.getPurchaseOrder());
        existentEgress.setConcept(updatedEgress.getConcept());
        existentEgress.setTotal(updatedEgress.getTotal());

        return toDTO(egressRepository.save(existentEgress));
    }

    public void deleteEgress(UUID id) {
        if (!egressRepository.existsById(id)) {
            throw new IllegalArgumentException("Egreso no encontrado con id: " + id);
        }
        egressRepository.deleteById(id);
    }

    public List<EgressDTO> getByConcept(String concept) {
        validateConcept(concept);
        return egressRepository.findByConcept(concept)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<EgressDTO> getByDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return egressRepository.findByDateBetween(startDate, endDate)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<EgressDTO> getByProvider(UUID providerId) {
        validateProvider(providerId);
        return egressRepository.findByProvider(providerId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<EgressDTO> getByPayMethod(UUID payMethodId) {
        return egressRepository.findByPayMethod(payMethodId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<EgressDTO> getByPurchaseOrder(UUID purchaseOrderId) {
        return egressRepository.findByPurchaseOrder(purchaseOrderId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private EgressDomain getEntityById(UUID id) {
        return egressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Egreso no encontrado con id: " + id));
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

    private void validateEgress(EgressDomain egress) {
        validateDate(egress.getDate());
        validateProvider(egress.getProvider());
        validatePurchaseOrder(egress.getPurchaseOrder());
        validateConcept(egress.getConcept());
        validateTotal(egress.getTotal());
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (date.isAfter(LocalDate.now(ZoneId.of("America/Bogota")))) {
            throw new IllegalArgumentException("La fecha no puede ser posterior a la fecha actual");
        }
    }

    private void validateProvider(UUID provider) {
        if (provider == null) {
            throw new IllegalArgumentException("El proveedor es obligatorio");
        }
    }

    private void validatePurchaseOrder(UUID purchaseOrder) {
        if (purchaseOrder == null) {
            throw new IllegalArgumentException("La orden de compra es obligatoria");
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
}