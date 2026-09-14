package com.uco.ucopetapi.service.appointment;

import com.uco.ucopetapi.domain.appointment.AppointmentDomain;
import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeDTO;
import com.uco.ucopetapi.repository.appointment.IAppointmentRepository;
import com.uco.ucopetapi.service.appointmentType.AppointmentTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final UUID MOCK_TUTOR_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    private static final UUID MOCK_PET_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    private static final UUID MOCK_DOCTOR_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

    private static final Map<UUID, String> MOCK_TUTORS = Map.of(
            MOCK_TUTOR_ID, "Simon"
    );
    private static final Map<UUID, String> MOCK_PETS = Map.of(
            MOCK_PET_ID, "Luna"
    );
    private static final Map<UUID, UUID> MOCK_PET_TUTORS = Map.of(
            MOCK_PET_ID, MOCK_TUTOR_ID
    );
    private static final Map<UUID, String> MOCK_DOCTORS = Map.of(
            MOCK_DOCTOR_ID, "Dra. Perez"
    );

    private final IAppointmentRepository appointmentRepository;
    private final AppointmentTypeService appointmentTypeService;

    public AppointmentService(
            IAppointmentRepository appointmentRepository,
            AppointmentTypeService appointmentTypeService) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentTypeService = appointmentTypeService;
    }

    public List<AppointmentDTO> findAll() {
        return appointmentRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public Optional<AppointmentDTO> findById(UUID id) {
        return appointmentRepository.findById(id).map(this::toDTO);
    }

    public List<AppointmentDTO> findByTutorId(UUID tutorId) {
        return appointmentRepository.findByTutorId(tutorId).stream()
                .map(this::toDTO)
                .toList();
    }

    public List<AppointmentDTO> findPendingByTutorId(UUID tutorId) {
        return appointmentRepository.findByTutorIdAndStatus(tutorId, "PENDING").stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public AppointmentDTO create(AppointmentDTO appointmentDTO) {
        AppointmentDomain appointment = toDomain(appointmentDTO);
        validateRelationships(appointment);
        appointment.setId(UUID.randomUUID());
        return toDTO(appointmentRepository.save(appointment));
    }

    @Transactional
    public Optional<AppointmentDTO> update(UUID id, AppointmentDTO appointmentDTO) {
        if (!appointmentRepository.existsById(id)) {
            return Optional.empty();
        }

        AppointmentDomain appointment = toDomain(appointmentDTO);
        validateRelationships(appointment);
        appointment.setId(id);
        return Optional.of(toDTO(appointmentRepository.save(appointment)));
    }

    @Transactional
    public boolean delete(UUID id) {
        if (!appointmentRepository.existsById(id)) {
            return false;
        }

        appointmentRepository.deleteById(id);
        return true;
    }

    private String findDoctorName(UUID doctorId) {
        if (doctorId == null) {
            return null;
        }

        return MOCK_DOCTORS.get(doctorId);
    }

    private String findPetName(UUID petId) {
        if (petId == null) {
            return null;
        }

        return MOCK_PETS.get(petId);
    }

    private String findTutorName(UUID tutorId) {
        if (tutorId == null) {
            return null;
        }

        return MOCK_TUTORS.get(tutorId);
    }

    private String findAppointmentTypeName(UUID appointmentTypeId) {
        if (appointmentTypeId == null) {
            return null;
        }

        return appointmentTypeService.findById(appointmentTypeId)
                .map(AppointmentTypeDTO::getName)
                .orElse(null);
    }

    private void validateRelationships(AppointmentDomain appointment) {
        validateDoctor(appointment.getDoctorId());
        validateTutorAndPet(appointment.getTutorId(), appointment.getPetId());
        validateAppointmentType(appointment.getAppointmentTypeId());
    }

    private void validateDoctor(UUID doctorId) {
        if (doctorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El doctorId es obligatorio para crear una cita");
        }

        if (!MOCK_DOCTORS.containsKey(doctorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un medico con el id indicado");
        }
    }

    private void validateTutorAndPet(UUID tutorId, UUID petId) {
        if (tutorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tutorId es obligatorio para crear una cita");
        }
        if (petId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El petId es obligatorio para crear una cita");
        }

        if (!MOCK_TUTORS.containsKey(tutorId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un tutor con el id indicado");
        }
        if (!MOCK_PETS.containsKey(petId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe una mascota con el id indicado");
        }

        if (!tutorId.equals(MOCK_PET_TUTORS.get(petId))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La mascota indicada no pertenece al tutor de la cita");
        }
    }

    private void validateAppointmentType(UUID appointmentTypeId) {
        if (appointmentTypeId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El appointmentTypeId es obligatorio para crear una cita");
        }
        if (!appointmentTypeService.existsById(appointmentTypeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un tipo de cita con el id indicado");
        }
    }

    private AppointmentDomain toDomain(AppointmentDTO appointmentDTO) {
        AppointmentDomain appointment = new AppointmentDomain();
        appointment.setTutorId(appointmentDTO.getTutorId());
        appointment.setPetId(appointmentDTO.getPetId());
        appointment.setDoctorId(appointmentDTO.getDoctorId());
        appointment.setAppointmentTypeId(appointmentDTO.getAppointmentTypeId());
        appointment.setAppointmentDate(appointmentDTO.getAppointmentDate());
        appointment.setAppointmentTime(appointmentDTO.getAppointmentTime());
        appointment.setStatus(appointmentDTO.getStatus());
        appointment.setCancellationReason(appointmentDTO.getCancellationReason());
        return appointment;
    }

    private AppointmentDTO toDTO(AppointmentDomain appointment) {
        AppointmentDTO appointmentDTO = new AppointmentDTO();
        appointmentDTO.setId(appointment.getId());
        appointmentDTO.setTutorId(appointment.getTutorId());
        appointmentDTO.setTutorName(findTutorName(appointment.getTutorId()));
        appointmentDTO.setPetId(appointment.getPetId());
        appointmentDTO.setPetName(findPetName(appointment.getPetId()));
        appointmentDTO.setDoctorId(appointment.getDoctorId());
        appointmentDTO.setDoctorName(findDoctorName(appointment.getDoctorId()));
        appointmentDTO.setAppointmentTypeId(appointment.getAppointmentTypeId());
        appointmentDTO.setAppointmentTypeName(findAppointmentTypeName(appointment.getAppointmentTypeId()));
        appointmentDTO.setAppointmentDate(appointment.getAppointmentDate());
        appointmentDTO.setAppointmentTime(appointment.getAppointmentTime());
        appointmentDTO.setStatus(appointment.getStatus());
        appointmentDTO.setCancellationReason(appointment.getCancellationReason());
        return appointmentDTO;
    }
}