package com.uco.ucopetapi.service.procedure;

import com.uco.ucopetapi.domain.procedure.ProcedureDomain;
import com.uco.ucopetapi.domain.procedure.ProcedureProductDomain;
import com.uco.ucopetapi.domain.procedure.mapper.ProcedureMapper;
import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.dto.procedure.ProcedureProductDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
import com.uco.ucopetapi.dto.space.SpaceDTO;
import com.uco.ucopetapi.repository.procedure.IProcedureRepository;
import com.uco.ucopetapi.service.procedure.exception.InvalidProcedureRequestException;
import com.uco.ucopetapi.service.procedure.exception.ProcedureConflictException;
import com.uco.ucopetapi.service.procedure.exception.ProcedureNotFoundException;
import com.uco.ucopetapi.service.product.ProductService;
import com.uco.ucopetapi.service.space.SpaceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProcedureService implements IProcedureService {

    private static final int MAX_CODE_LENGTH = 50;
    private static final int MAX_TYPE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_DURATION_MINUTES = 1440;
    private static final int MAX_CODE_SEQUENCE = 999999;
    private static final String NUMERIC_CODE_REGEX = "^\\d+$";

    private final IProcedureRepository procedureRepository;
    private final SpaceService spaceService;
    private final ProcedureMapper procedureMapper;
    private final ProductService productService;

    public ProcedureService(final IProcedureRepository procedureRepository, final SpaceService spaceService,
                            final ProcedureMapper procedureMapper, final ProductService productService) {
        this.procedureRepository = procedureRepository;
        this.spaceService = spaceService;
        this.procedureMapper = procedureMapper;
        this.productService = productService;
    }

    @Override
    public List<ProcedureDTO> findAll() {
        return procedureMapper.toDTOList(procedureRepository.findAll());
    }

    @Override
    public List<ProcedureDTO> findActiveProcedures() {
        return procedureMapper.toDTOList(procedureRepository.findByActive(true));
    }

    @Override
    public List<ProcedureDTO> findInactiveProcedures() {
        return procedureMapper.toDTOList(procedureRepository.findByActive(false));
    }

    @Override
    public List<SpaceDTO> findActiveSpacesForProcedureCreation() {
        return spaceService.getSpacesByStatus(true).stream()
                .map(space -> new SpaceDTO(space.getId(), space.getCode(), space.getType(),
                        space.getDescription(), space.getActive()))
                .toList();
    }

    @Override
    public ProcedureDTO findById(final UUID id) {
        return procedureMapper.toDTO(findEntityById(id));
    }

    @Override
    public ProcedureDTO findByCode(final String code) {
        final String normalizedCode = normalizeCode(code);
        validateCodeFormat(normalizedCode);
        return procedureRepository.findByCode(normalizedCode)
                .map(procedureMapper::toDTO)
                .orElseThrow(() -> new ProcedureNotFoundException(
                        "No se encontró el procedimiento solicitado"));
    }

    @Override
    @Transactional
    public ProcedureDTO create(final ProcedureDTO request) {
        validateRequest(request);
        validateSpaceIsActive(request.getSpaceId());
        final String code = resolveCodeForCreate(request.getCode());
        validateCodeIsAvailable(code, null);
        final ProcedureDomain procedure = new ProcedureDomain(code, clean(request.getType()),
                clean(request.getDescription()), request.getDurationMinutes(), request.getSpaceId(),
                buildDefaultProducts(request.getDefaultProducts()), true);
        return procedureMapper.toDTO(procedureRepository.save(procedure));
    }

    @Override
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
        procedure.setDefaultProducts(buildDefaultProducts(request.getDefaultProducts()));
        return procedureMapper.toDTO(procedureRepository.save(procedure));
    }

    @Override
    @Transactional
    public ProcedureDTO deactivate(final UUID id) {
        return updateStatus(id, false);
    }

    @Override
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
                .orElseThrow(() -> new ProcedureNotFoundException(
                        "No se encontró el procedimiento solicitado"));
    }

    private void validateRequest(final ProcedureDTO request) {
        if (request == null) {
            throw new InvalidProcedureRequestException("La información del procedimiento es obligatoria");
        }

        final String code = normalizeCode(request.getCode());
        validateCodeFormat(code);

        validateRequiredText(request.getType(), "El tipo es obligatorio",
                "El tipo no puede superar los 100 caracteres", MAX_TYPE_LENGTH);
        validateRequiredText(request.getDescription(), "La descripción es obligatoria",
                "La descripción no puede superar los 500 caracteres", MAX_DESCRIPTION_LENGTH);
        validateDuration(request.getDurationMinutes());
        validateIdValue(request.getSpaceId(), "El espacio es obligatorio");
        validateProducts(request.getDefaultProducts());
    }

    private void validateCodeFormat(final String code) {
        if (code != null && code.length() > MAX_CODE_LENGTH) {
            throw new InvalidProcedureRequestException("El código no puede superar los 50 caracteres");
        }
        if (code != null && !code.matches(NUMERIC_CODE_REGEX)) {
            throw new InvalidProcedureRequestException("El código debe ser un número entero positivo");
        }
        if (code != null) {
            validateCodeRange(code);
        }
    }

    private void validateCodeRange(final String code) {
        try {
            final long numericCode = Long.parseLong(code);
            if (numericCode <= 0) {
                throw new InvalidProcedureRequestException("El código debe ser mayor que cero");
            }
            if (numericCode > MAX_CODE_SEQUENCE) {
                throw new InvalidProcedureRequestException("El código no puede superar " + MAX_CODE_SEQUENCE);
            }
        } catch (NumberFormatException _) {
            throw new InvalidProcedureRequestException("El código no tiene un formato válido");
        }
    }

    private void validateRequiredText(final String value, final String requiredMessage,
                                      final String maxLengthMessage, final int maxLength) {
        if (value == null || value.isBlank()) {
            throw new InvalidProcedureRequestException(requiredMessage);
        }
        if (clean(value).length() > maxLength) {
            throw new InvalidProcedureRequestException(maxLengthMessage);
        }
    }

    private void validateDuration(final Integer durationMinutes) {
        if (durationMinutes == null) {
            throw new InvalidProcedureRequestException("La duración en minutos es obligatoria");
        }
        if (durationMinutes <= 0) {
            throw new InvalidProcedureRequestException("La duración en minutos debe ser mayor que cero");
        }
        if (durationMinutes > MAX_DURATION_MINUTES) {
            throw new InvalidProcedureRequestException("La duración en minutos no puede superar 1440");
        }
    }

    private void validateId(final UUID id) {
        validateIdValue(id, "El identificador del procedimiento es obligatorio");
    }

    private void validateIdValue(final UUID id, final String message) {
        if (id == null) {
            throw new InvalidProcedureRequestException(message);
        }
    }

    private void validateSpaceIsActive(final UUID spaceId) {
        final boolean existsActiveSpace = spaceService.getSpacesByStatus(true).stream()
                .anyMatch(space -> Objects.equals(space.getId(), spaceId));
        if (!existsActiveSpace) {
            throw new InvalidProcedureRequestException(
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
                    throw new ProcedureConflictException("Ya existe un procedimiento con el código indicado");
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
            throw new ProcedureConflictException("No hay códigos disponibles para nuevos procedimientos");
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
            throw new InvalidProcedureRequestException("Debes asociar al menos un producto predeterminado");
        }
        final Set<UUID> productIds = new HashSet<>();
        for (ProcedureProductDTO product : products) {
            validateProduct(product, productIds);
        }
    }

    private void validateProduct(final ProcedureProductDTO product, final Set<UUID> productIds) {
        if (product == null) {
            throw new InvalidProcedureRequestException(
                    "La información del producto predeterminado es obligatoria");
        }
        validateProductId(product.getProductId());
        validateProductQuantity(product.getQuantity());
        validateProductIsNotDuplicated(product.getProductId(), productIds);
    }

    private void validateProductId(final UUID productId) {
        if (productId == null) {
            throw new InvalidProcedureRequestException(
                    "El identificador del producto predeterminado es obligatorio");
        }
    }

    private void validateProductQuantity(final Integer quantity) {
        if (quantity == null) {
            throw new InvalidProcedureRequestException("La cantidad es obligatoria");
        }
        if (quantity <= 0) {
            throw new InvalidProcedureRequestException("La cantidad debe ser mayor que cero");
        }
    }

    private void validateProductIsNotDuplicated(final UUID productId, final Set<UUID> productIds) {
        if (!productIds.add(productId)) {
            throw new InvalidProcedureRequestException(
                    "Un producto predeterminado solo puede asociarse una vez");
        }
    }

    private List<ProcedureProductDomain> buildDefaultProducts(final List<ProcedureProductDTO> products) {
        return products.stream()
                .map(item -> {
                    validateProductExists(item.getProductId());
                    return new ProcedureProductDomain(item.getProductId(), item.getQuantity());
                })
                .toList();
    }

    private void validateProductExists(final UUID productId) {
        final ProductDTO product;
        try {
            product = productService.getById(productId, null);
        } catch (NoSuchElementException _) {
            throw new InvalidProcedureRequestException("El producto con id " + productId + " no existe");
        }
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new InvalidProcedureRequestException(
                    "El producto con id " + productId + " no se encuentra activo");
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