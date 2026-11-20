package com.uco.ucopetapi.service.specialtie;

import com.uco.ucopetapi.domain.specialtie.SpecialtieDomain;
import com.uco.ucopetapi.domain.specialtie.mapper.SpecialtieMapper;
import com.uco.ucopetapi.dto.specialtie.SpecialtieDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.repository.certificate.ICertificateRepository;
import com.uco.ucopetapi.repository.specialtie.ISpecialtieRepository;
import com.uco.ucopetapi.service.specialtie.exception.SpecialtieConflictException;
import com.uco.ucopetapi.service.specialtie.exception.SpecialtieNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SpecialtieService {

    private final ISpecialtieRepository iSpecialtieRepository;
    private final SpecialtieMapper specialtieMapper;
    private final ICertificateRepository iCertificateRepository;

    public SpecialtieService(ISpecialtieRepository iSpecialtieRepository,
                             SpecialtieMapper specialtieMapper, ICertificateRepository iCertificateRepository) {
        this.iSpecialtieRepository = iSpecialtieRepository;
        this.specialtieMapper = specialtieMapper;
        this.iCertificateRepository = iCertificateRepository;
    }

    public List<SpecialtieDTO> getAllSpecialties() {

        List<SpecialtieDomain> domains = iSpecialtieRepository.findAll();
        return specialtieMapper.toDTOList(domains);
    }

    public SpecialtieDTO findSpecialtieById(UUID id) {
        SpecialtieDomain domain = iSpecialtieRepository.findById(id)
                .orElseThrow(() -> new SpecialtieNotFoundException("No se encontró la especialidad con el ID: " + id));

        return specialtieMapper.toDTO(domain);
    }

    public SpecialtieDTO findSpecialtieByName(String name) {
        SpecialtieDomain domain = iSpecialtieRepository.findSpecialtieByName(name)
                .orElseThrow(() -> new SpecialtieNotFoundException("No se encontró la especialidad con el nombre: " + name));

        return specialtieMapper.toDTO(domain);
    }

    @Transactional
    public SpecialtieDomain createNewSpecialtie(SpecialtieDTO dto) {

        validateFields(dto);

        validateSpecialtieNameIsFree(dto.getName(), null);

        SpecialtieDomain domainToSave = specialtieMapper.toDomain(dto);

        if (dto.getCertificate() != null) {
            validateCertificateExists(dto.getCertificate());
            domainToSave.setCertificate(iCertificateRepository.getReferenceById(dto.getCertificate()));
        }

        return iSpecialtieRepository.save(domainToSave);
    }

    @Transactional
    public SpecialtieDomain patchSpecialtie(UUID id, SpecialtieDTO dto) {

        if (dto == null) {
            throw new BusinessException("La información de la especialidad no puede ser nula.");
        }

        SpecialtieDomain existingSpecialtie = iSpecialtieRepository.findById(id)
                .orElseThrow(() -> new SpecialtieNotFoundException(
                        "No se encontró ninguna especialidad con el ID: " + id
                ));

        if (dto.getName() != null) {
            validateName(dto.getName());
            validateSpecialtieNameIsFree(dto.getName(), id);
            existingSpecialtie.setName(dto.getName());
        }

        if (dto.getDescription() != null) {
            validateDescription(dto.getDescription());
            existingSpecialtie.setDescription(dto.getDescription());
        }

        if (dto.isActive() != null) {
            existingSpecialtie.setActive(dto.isActive());
        }

        return iSpecialtieRepository.save(existingSpecialtie);
    }

    private static final String NAME_REGEX = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$";
    private static final String DESCRIPTION_REGEX = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ .,;:()/-]+$";
    private static final String DESCRIPTION_CHARS_MESSAGE =
            "La descripción solo puede contener letras, números, espacios y los signos . , ; : ( ) - /";
    private void validateFields(SpecialtieDTO dto) {
        if (dto == null) {
            throw new BusinessException("La información de la especialidad no puede ser nula.");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new BusinessException("El nombre de la especialidad es obligatorio.");
        }

        if (!dto.getName().matches(NAME_REGEX)) {
            throw new BusinessException(
                    "El nombre solo puede contener letras y espacios."
            );
        }

        String nameTrimmed = dto.getName().trim();
        if (nameTrimmed.length() < 5 || nameTrimmed.length() > 40) {
            throw new BusinessException("El nombre debe tener una longitud de entre 5 y 40 caracteres.");
        }

        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new BusinessException("La descripción es obligatoria.");
        }

        if (!dto.getDescription().matches(DESCRIPTION_REGEX)) {
            throw new BusinessException(DESCRIPTION_CHARS_MESSAGE);
        }

        String descTrimmed = dto.getDescription().trim();
        if (descTrimmed.length() < 10 || descTrimmed.length() > 150) {
            throw new BusinessException("La descripción debe tener una longitud entre 10 y 150 caracteres.");
        }
    }

    private void validateCertificateExists(UUID certificateId) {
        boolean exists = iCertificateRepository.existsById(certificateId);
        if (!exists) {
            throw new BusinessException("El certificado especificado no existe en la base de datos.");
        }
    }

    /**
     * Lanza 409 si ya existe otra especialidad con ese nombre.
     * {@code currentId} es el id de la especialidad que se está editando (null al crear).
     */
    private void validateSpecialtieNameIsFree(String name, UUID currentId) {
        iSpecialtieRepository.findSpecialtieByName(name)
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new SpecialtieConflictException(
                            "Ya existe una especialidad con el nombre: " + name
                    );
                });
    }

    private void validateName(String name) {
        String nameTrimmed = name.trim();
        if (nameTrimmed.length() < 5 || nameTrimmed.length() > 40) {
            throw new BusinessException("El nombre debe tener una longitud de entre 5 y 40 caracteres.");
        }

        if (!name.matches(NAME_REGEX)) {
            throw new BusinessException(
                    "El nombre solo puede contener letras y espacios."
            );
        }
    }

    private void validateDescription(String description) {
        String descriptionTrimmed = description.trim();
        if (descriptionTrimmed.length() < 10 || descriptionTrimmed.length() > 150) {
            throw new BusinessException("La descripción debe tener una longitud de entre 10 y 150 caracteres.");
        }

        if (!description.matches(DESCRIPTION_REGEX)) {
            throw new BusinessException(DESCRIPTION_CHARS_MESSAGE);
        }
    }
}
