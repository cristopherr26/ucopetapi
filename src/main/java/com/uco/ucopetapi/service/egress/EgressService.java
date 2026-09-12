package com.uco.ucopetapi.service.egress;

import com.uco.ucopetapi.domain.egress.EgressDomain;
import com.uco.ucopetapi.repository.egress.EgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class EgressService {

    private final EgressRepository egressRepository;

    public EgressService(EgressRepository egressRepository) {
        this.egressRepository = egressRepository;
    }

    public List<EgressDomain> obtenerTodos() {
        return egressRepository.findAll();
    }

    public EgressDomain obtenerPorId(UUID id) {
        return egressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Egreso no encontrado con id: " + id));
    }

    public EgressDomain guardar(EgressDomain egress) {
        return egressRepository.save(egress);
    }

    public EgressDomain actualizar(UUID id, EgressDomain egressActualizado) {
        EgressDomain egressExistente = obtenerPorId(id);

        if (egressActualizado.getTotal() != null && egressActualizado.getTotal() < 0) {
            throw new IllegalArgumentException("El total no puede ser negativo");
        }

        egressExistente.setDate(egressActualizado.getDate());
        egressExistente.setProvider(egressActualizado.getProvider());
        egressExistente.setPayMethod(egressActualizado.getPayMethod());
        egressExistente.setPurchaseOrder(egressActualizado.getPurchaseOrder());
        egressExistente.setConcept(egressActualizado.getConcept());
        egressExistente.setTotal(egressActualizado.getTotal());

        return egressRepository.save(egressExistente);
    }

    public void eliminar(UUID id) {
        if (!egressRepository.existsById(id)) {
            throw new RuntimeException("Egreso no encontrado con id: " + id);
        }
        egressRepository.deleteById(id);
    }

    public List<EgressDomain> buscarPorConcepto(String concept) {
        return egressRepository.findByConcept(concept);
    }

    public List<EgressDomain> buscarPorRangoDeFechas(LocalDate startDate, LocalDate endDate) {
        return egressRepository.findByDateBetween(startDate, endDate);
    }

    public List<EgressDomain> buscarPorProvider(UUID providerId) {
        return egressRepository.findByProvider(providerId);
    }

    public List<EgressDomain> buscarPorPayMethod(UUID payMethodId) {
        return egressRepository.findByPayMethod(payMethodId);
    }
}
