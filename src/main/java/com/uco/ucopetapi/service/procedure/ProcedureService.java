package com.uco.ucopetapi.service.procedure;

import com.uco.ucopetapi.domain.procedure.ProcedureDomain;
import com.uco.ucopetapi.domain.procedure.ProcedureProductDomain;
import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.dto.procedure.ProcedureProductDTO;
import com.uco.ucopetapi.repository.procedure.IProcedureRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProcedureService {

    private final IProcedureRepository procedureRepository;

    public ProcedureService(final IProcedureRepository procedureRepository) {
        this.procedureRepository = procedureRepository;
    }

    public List<ProcedureDTO> findAll() {
        return procedureRepository.findAll().stream().map(this::toDto).toList();
    }

    public ProcedureDTO findById(final UUID id) {
        return toDto(findEntityById(id));
    }

    @Transactional
    public ProcedureDTO create(final ProcedureDTO request) {
        validateProducts(request.getDefaultProducts());
        final String code = normalizeCode(request.getCode());
        validateCodeIsAvailable(code, null);
        final ProcedureDomain procedure = new ProcedureDomain(code, request.getType().trim(),
                request.getDescription().trim(), request.getDurationMinutes(),
                toDomainProducts(request.getDefaultProducts()), request.getActive());
        return toDto(procedureRepository.save(procedure));
    }

    @Transactional
    public ProcedureDTO update(final UUID id, final ProcedureDTO request) {
        validateProducts(request.getDefaultProducts());
        final ProcedureDomain procedure = findEntityById(id);
        final String code = normalizeCode(request.getCode());
        validateCodeIsAvailable(code, id);
        procedure.setCode(code);
        procedure.setType(request.getType().trim());
        procedure.setDescription(request.getDescription().trim());
        procedure.setDurationMinutes(request.getDurationMinutes());
        procedure.setDefaultProducts(toDomainProducts(request.getDefaultProducts()));
        procedure.setActive(request.getActive());
        return toDto(procedureRepository.save(procedure));
    }

    @Transactional
    public ProcedureDTO deactivate(final UUID id) {
        final ProcedureDomain procedure = findEntityById(id);
        procedure.setActive(false);
        return toDto(procedureRepository.save(procedure));
    }

    private ProcedureDomain findEntityById(final UUID id) {
        return procedureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Procedure not found"));
    }

    private void validateCodeIsAvailable(final String code, final UUID currentId) {
        if (code == null) {
            return;
        }
        procedureRepository.findByCode(code)
                .filter(procedure -> !procedure.getId().equals(currentId))
                .ifPresent(procedure -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Procedure code already exists");
                });
    }

    private void validateProducts(final List<ProcedureProductDTO> products) {
        final Set<UUID> productIds = new HashSet<>();
        if (products.stream().map(ProcedureProductDTO::getProductId).anyMatch(id -> !productIds.add(id))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A default product can only be included once");
        }
    }

    private List<ProcedureProductDomain> toDomainProducts(final List<ProcedureProductDTO> products) {
        return products.stream().map(product -> new ProcedureProductDomain(product.getProductId(),
                product.getQuantity())).toList();
    }

    private ProcedureDTO toDto(final ProcedureDomain procedure) {
        return new ProcedureDTO(procedure.getId(), procedure.getCode(), procedure.getType(),
                procedure.getDescription(), procedure.getDurationMinutes(),
                procedure.getDefaultProducts().stream().map(product -> new ProcedureProductDTO(
                        product.getProductId(), product.getQuantity())).toList(), procedure.isActive());
    }

    private String normalizeCode(final String code) {
        return code == null || code.isBlank() ? null : code.trim();
    }
}
