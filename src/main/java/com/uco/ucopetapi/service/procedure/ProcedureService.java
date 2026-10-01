package com.uco.ucopetapi.service.procedure;

import com.uco.ucopetapi.domain.procedure.ProcedureDomain;
import com.uco.ucopetapi.domain.procedure.mapper.ProcedureMapper;
import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.dto.procedure.ProcedureProductDTO;
import com.uco.ucopetapi.dto.space.SpaceDTO;
import com.uco.ucopetapi.repository.procedure.IProcedureRepository;
import com.uco.ucopetapi.service.space.SpaceService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProcedureService {

    private static final int MAX_CODE_LENGTH = 50;
    private static final int MAX_TYPE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_DURATION_MINUTES = 1440;
    private static final int MAX_PRODUCT_CODE_LENGTH = 20;
    private static final int MAX_CODE_SEQUENCE = 999999;
    private static final String NUMERIC_CODE_REGEX = "^\\d+$";

    private final IProcedureRepository procedureRepository;
    private final SpaceService spaceService;
    private final ProcedureMapper procedureMapper;

    public ProcedureService(final IProcedureRepository procedureRepository, final SpaceService spaceService,
                            final ProcedureMapper procedureMapper) {
        this.procedureRepository = procedureRepository;
        this.spaceService = spaceService;
        this.procedureMapper = procedureMapper;
    }

    public List<ProcedureDTO> findAll() {
        return procedureMapper.toDTOList(procedureRepository.findAll());
    }

    public List<ProcedureDTO> findActiveProcedures() {
        return procedureMapper.toDTOList(procedureRepository.findByActive(true));
    }

    public List<ProcedureDTO> findInactiveProcedures() {
        return procedureMapper.toDTOList(procedureRepository.findByActive(false));
    }

    public List<SpaceDTO> findActiveSpacesForProcedureCreation() {
        return spaceService.getSpacesByStatus(true).stream()
                .map(space -> new SpaceDTO(space.getId(), space.getCode(), space.getType(),
                        space.getDescription(), space.getActive()))
                .toList();
    }

    public ProcedureDTO findById(final UUID id) {
        return procedureMapper.toDTO(findEntityById(id));
    }

    public ProcedureDTO findByCode(final String code) {
        final String normalizedCode = normalizeCode(code);
        validateCodeFormat(normalizedCode);
        return procedureRepository.findByCode(normalizedCode)
                .map(procedureMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontro el procedimiento solicitado"));
    }

    @Transactional
    public ProcedureDTO create(final ProcedureDTO request) {
        validateRequest(request);
        validateSpaceIsActive(request.getSpaceId());
        final String code = resolveCodeForCreate(request.getCode());
        validateCodeIsAvailable(code, null);
        final ProcedureDomain procedure = new ProcedureDomain(code, clean(request.getType()),
                clean(request.getDescription()), request.getDurationMinutes(), request.getSpaceId(),
                procedureMapper.toProductDomainList(request.getDefaultProducts()), true);
        return procedureMapper.toDTO(procedureRepository.save(procedure));
    }

    @Transactional
    public ProcedureDTO update(final UUID id, final ProcedureDTO request) {
        validateId(id);
        validateRequest(request);
        validateSpaceIsActive(request.getSpaceId());
        final ProcedureDomain procedure = findEntityById(id);
        final String code = resolveCodeForUpdate(request.getCode(), procedure.getCode());
        validateCodeIsAvailable(code, id);
        procedure.setCode(code);
        procedure.setType(clean(request.getType()));
        procedure.setDescription(clean(request.getDescription()));
        procedure.setDurationMinutes(request.getDurationMinutes());
        procedure.setSpaceId(request.getSpaceId());
        procedure.setDefaultProducts(procedureMapper.toProductDomainList(request.getDefaultProducts()));
        return procedureMapper.toDTO(procedureRepository.save(procedure));
    }

    @Transactional
    public ProcedureDTO deactivate(final UUID id) {
        return updateStatus(id, false);
    }

    @Transactional
    public ProcedureDTO activate(final UUID id) {
        return updateStatus(id, true);
    }

    private ProcedureDTO updateStatus(final UUID id, final boolean active) {
        validateId(id);
        final ProcedureDomain procedure = findEntityById(id);
        procedure.setActive(active);
        return procedureMapper.toDTO(procedureRepository.save(procedure));
    }

    private ProcedureDomain findEntityById(final UUID id) {
        return procedureRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontro el procedimiento solicitado"));
    }

    private void validateRequest(final ProcedureDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La informacion del procedimiento es obligatoria");
        }

        final String code = normalizeCode(request.getCode());
        validateCodeFormat(code);

        validateRequiredText(request.getType(), "El tipo es obligatorio", "El tipo no puede superar los 100 caracteres",
                MAX_TYPE_LENGTH);
        validateRequiredText(request.getDescription(), "La descripcion es obligatoria",
                "La descripcion no puede superar los 500 caracteres", MAX_DESCRIPTION_LENGTH);
        validateDuration(request.getDurationMinutes());
        validateIdValue(request.getSpaceId(), "El espacio es obligatorio");
        validateProducts(request.getDefaultProducts());
    }

    private void validateCodeFormat(final String code) {
        if (code != null && code.length() > MAX_CODE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo no puede superar los 50 caracteres");
        }
        if (code != null && !code.matches(NUMERIC_CODE_REGEX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo debe ser un numero entero positivo");
        }
        if (code != null) {
            validateCodeRange(code);
        }
    }

    private void validateCodeRange(final String code) {
        try {
            final long numericCode = Long.parseLong(code);
            if (numericCode <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El codigo debe ser mayor que cero");
            }
            if (numericCode > MAX_CODE_SEQUENCE) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El codigo no puede superar " + MAX_CODE_SEQUENCE);
            }
        } catch (NumberFormatException _) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo no tiene un formato valido");
        }
    }

    private void validateRequiredText(final String value, final String requiredMessage,
                                      final String maxLengthMessage, final int maxLength) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, requiredMessage);
        }
        if (clean(value).length() > maxLength) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, maxLengthMessage);
        }
    }

    private void validateDuration(final Integer durationMinutes) {
        if (durationMinutes == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La duracion en minutos es obligatoria");
        }
        if (durationMinutes <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La duracion en minutos debe ser mayor que cero");
        }
        if (durationMinutes > MAX_DURATION_MINUTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La duracion en minutos no puede superar 1440");
        }
    }

    private void validateId(final UUID id) {
        validateIdValue(id, "El identificador del procedimiento es obligatorio");
    }

    private void validateIdValue(final UUID id, final String message) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validateSpaceIsActive(final UUID spaceId) {
        final boolean existsActiveSpace = spaceService.getSpacesByStatus(true).stream()
                .anyMatch(space -> Objects.equals(space.getId(), spaceId));
        if (!existsActiveSpace) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El espacio seleccionado no existe o no se encuentra activo");
        }
    }

    private void validateCodeIsAvailable(final String code, final UUID currentId) {
        if (code == null) {
            return;
        }
        procedureRepository.findByCode(code)
                .filter(procedure -> !procedure.getId().equals(currentId))
                .ifPresent(procedure -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Ya existe un procedimiento con el codigo indicado");
                });
    }

    private String resolveCodeForCreate(final String requestedCode) {
        final String code = normalizeCode(requestedCode);
        return code == null ? generateNextCode() : code;
    }

    private String resolveCodeForUpdate(final String requestedCode, final String currentCode) {
        final String code = normalizeCode(requestedCode);
        final String storedCode = normalizeCode(currentCode);
        return code == null ? resolveCodeForCreate(storedCode) : code;
    }

    private String generateNextCode() {
        final int nextSequence = procedureRepository.findAll().stream()
                .map(ProcedureDomain::getCode)
                .map(this::extractCodeSequence)
                .max(Integer::compareTo)
                .orElse(0) + 1;
        if (nextSequence > MAX_CODE_SEQUENCE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No hay codigos disponibles para nuevos procedimientos");
        }
        return String.valueOf(nextSequence);
    }

    private int extractCodeSequence(final String code) {
        if (code == null || !code.matches(NUMERIC_CODE_REGEX)) {
            return 0;
        }
        try {
            return Integer.parseInt(code);
        } catch (NumberFormatException _) {
            return 0;
        }
    }

    private void validateProducts(final List<ProcedureProductDTO> products) {
        if (products == null || products.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debes asociar al menos un producto predeterminado");
        }
        final Set<String> productCodes = new HashSet<>();
        for (ProcedureProductDTO product : products) {
            validateProduct(product, productCodes);
        }
    }

    private void validateProduct(final ProcedureProductDTO product, final Set<String> productCodes) {
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La informacion del producto predeterminado es obligatoria");
        }
        validateProductId(product.getProductId());
        final String productCode = validateProductCode(product.getProductCode());
        validateProductQuantity(product.getQuantity());
        validateProductIsNotDuplicated(productCode, productCodes);
    }

    private void validateProductId(final UUID productId) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El identificador del producto predeterminado es obligatorio");
        }
    }

    private String validateProductCode(final String requestedProductCode) {
        final String productCode = clean(requestedProductCode);
        if (productCode == null || productCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo del producto predeterminado es obligatorio");
        }
        if (productCode.length() > MAX_PRODUCT_CODE_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El codigo del producto predeterminado no puede superar los 20 caracteres");
        }
        return productCode;
    }

    private void validateProductQuantity(final Integer quantity) {
        if (quantity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad es obligatoria");
        }
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cantidad debe ser mayor que cero");
        }
    }

    private void validateProductIsNotDuplicated(final String productCode, final Set<String> productCodes) {
        if (!productCodes.add(productCode.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un producto predeterminado solo puede asociarse una vez");
        }
    }

    private String normalizeCode(final String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        final String trimmedCode = code.trim();
        if (trimmedCode.matches(NUMERIC_CODE_REGEX)) {
            return normalizeNumericCode(trimmedCode);
        }
        return trimmedCode;
    }

    private String normalizeNumericCode(final String code) {
        try {
            return String.valueOf(Long.parseLong(code));
        } catch (NumberFormatException _) {
            return code;
        }
    }

    private String clean(final String value) {
        return value == null ? null : value.trim();
    }
}
