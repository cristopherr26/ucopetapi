package com.uco.ucopetapi.service.appointmentType;

import com.uco.ucopetapi.domain.appointmentType.AppointmentTypeDomain;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeDTO;
import com.uco.ucopetapi.repository.appointmentType.IAppointmentTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentTypeService {

    private final IAppointmentTypeRepository appointmentTypeRepository;

    public AppointmentTypeService(IAppointmentTypeRepository appointmentTypeRepository) {
        this.appointmentTypeRepository = appointmentTypeRepository;
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

    @Transactional
    public AppointmentTypeDTO create(AppointmentTypeDTO appointmentTypeDTO) {
        AppointmentTypeDomain appointmentType = toDomain(appointmentTypeDTO);
        appointmentType.setId(UUID.randomUUID());
        return toDTO(appointmentTypeRepository.save(appointmentType));
    }

    @Transactional
    public Optional<AppointmentTypeDTO> update(UUID id, AppointmentTypeDTO appointmentTypeDTO) {
        if (!appointmentTypeRepository.existsById(id)) {
            return Optional.empty();
        }

        AppointmentTypeDomain appointmentType = toDomain(appointmentTypeDTO);
        appointmentType.setId(id);
        return Optional.of(toDTO(appointmentTypeRepository.save(appointmentType)));
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!appointmentTypeRepository.existsById(id)) {
            return false;
        }

        appointmentTypeRepository.deleteById(id);
        return true;
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