package com.uco.ucopetapi.service.appointmentType;

import com.uco.ucopetapi.domain.appointmentType.AppointmentTypeDomain;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeDTO;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeStatusDTO;
import com.uco.ucopetapi.repository.appointment.IAppointmentRepository;
import com.uco.ucopetapi.repository.appointmentType.IAppointmentTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentTypeService {

    private static final int NAME_MAX_LENGTH = 100;
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    private final IAppointmentTypeRepository appointmentTypeRepository;
    private final IAppointmentRepository appointmentRepository;

    public AppointmentTypeService(IAppointmentTypeRepository appointmentTypeRepository,
                                  IAppointmentRepository appointmentRepository) {
        this.appointmentTypeRepository = appointmentTypeRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public List<AppointmentTypeDTO> findAll() {
        return appointmentTypeRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<AppointmentTypeDTO> findById(UUID id) {
        return appointmentTypeRepository.findById(id).map(this::toDTO);
    }

    public List<AppointmentTypeDTO> findAllActive() {
        return appointmentTypeRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .toList();
    }

    public boolean existsById(UUID id) {
        return id != null && appointmentTypeRepository.existsById(id);
    }

    public boolean isActive(UUID id) {
        return findById(id)
                .map(AppointmentTypeDTO::getIsActive)
                .orElse(false);
    }

    @Transactional
    public AppointmentTypeDTO create(AppointmentTypeDTO appointmentTypeDTO) {
        validateAndNormalize(appointmentTypeDTO, null);
        // Si no se indica el estado, un tipo nuevo nace activo.
        if (appointmentTypeDTO.getIsActive() == null) {
            appointmentTypeDTO.setIsActive(true);
        }
        AppointmentTypeDomain appointmentType = toDomain(appointmentTypeDTO);
        appointmentType.setId(UUID.randomUUID());
        return toDTO(appointmentTypeRepository.save(appointmentType));
    }

    @Transactional
    public Optional<AppointmentTypeDTO> update(UUID id, AppointmentTypeDTO appointmentTypeDTO) {
        Optional<AppointmentTypeDomain> stored = appointmentTypeRepository.findById(id);
        if (stored.isEmpty()) {
            return Optional.empty();
        }

        validateAndNormalize(appointmentTypeDTO, id);
        // Si no se indica el estado se conserva el que ya tenia.
        if (appointmentTypeDTO.getIsActive() == null) {
            appointmentTypeDTO.setIsActive(stored.get().getIsActive());
        }
        AppointmentTypeDomain appointmentType = toDomain(appointmentTypeDTO);
        appointmentType.setId(id);
        return Optional.of(toDTO(appointmentTypeRepository.save(appointmentType)));
    }

    @Transactional
    public Optional<AppointmentTypeDTO> updateStatus(UUID id, AppointmentTypeStatusDTO appointmentTypeStatusDTO) {
        if (appointmentTypeStatusDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos del estado del tipo de cita son obligatorios");
        }

        return appointmentTypeRepository.findById(id)
                .map(appointmentType -> {
                    if (appointmentTypeStatusDTO.getIsActive() == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "El estado activo del tipo de cita es obligatorio");
                    }

                    appointmentType.setIsActive(appointmentTypeStatusDTO.getIsActive());
                    return toDTO(appointmentTypeRepository.save(appointmentType));
                });
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!appointmentTypeRepository.existsById(id)) {
            return false;
        }
        // Borrarlo dejaria citas apuntando a un tipo que ya no existe: se debe desactivar.
        if (appointmentRepository.existsByAppointmentTypeId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede eliminar el tipo de cita porque tiene citas asociadas; desactivelo en su lugar");
        }

        appointmentTypeRepository.deleteById(id);
        return true;
    }

    // Nombre y descripcion obligatorios (sin espacios sobrantes), con el largo de la columna, y sin repetir
    // el nombre de otro tipo. Deja el DTO ya normalizado. currentId es el tipo que se edita (null al crear).
    private void validateAndNormalize(AppointmentTypeDTO appointmentTypeDTO, UUID currentId) {
        if (appointmentTypeDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos del tipo de cita son obligatorios");
        }

        String name = appointmentTypeDTO.getName() == null ? "" : appointmentTypeDTO.getName().trim();
        if (name.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del tipo de cita es obligatorio");
        }
        if (name.length() > NAME_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del tipo de cita no puede superar " + NAME_MAX_LENGTH + " caracteres");
        }

        String description = appointmentTypeDTO.getDescription() == null
                ? ""
                : appointmentTypeDTO.getDescription().trim();
        if (description.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descripcion del tipo de cita es obligatoria");
        }
        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La descripcion del tipo de cita no puede superar " + DESCRIPTION_MAX_LENGTH + " caracteres");
        }

        boolean duplicated = currentId == null
                ? appointmentTypeRepository.existsByNameIgnoreCase(name)
                : appointmentTypeRepository.existsByNameIgnoreCaseAndIdNot(name, currentId);
        if (duplicated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un tipo de cita con ese nombre");
        }

        appointmentTypeDTO.setName(name);
        appointmentTypeDTO.setDescription(description);
    }

    private AppointmentTypeDomain toDomain(AppointmentTypeDTO appointmentTypeDTO) {
        AppointmentTypeDomain appointmentType = new AppointmentTypeDomain();
        appointmentType.setName(appointmentTypeDTO.getName());
        appointmentType.setDescription(appointmentTypeDTO.getDescription());
        appointmentType.setIsActive(appointmentTypeDTO.getIsActive());
        return appointmentType;
    }

    private AppointmentTypeDTO toDTO(AppointmentTypeDomain appointmentType) {
        AppointmentTypeDTO appointmentTypeDTO = new AppointmentTypeDTO();
        appointmentTypeDTO.setId(appointmentType.getId());
        appointmentTypeDTO.setName(appointmentType.getName());
        appointmentTypeDTO.setDescription(appointmentType.getDescription());
        appointmentTypeDTO.setIsActive(appointmentType.getIsActive());
        return appointmentTypeDTO;
    }
}