package com.uco.ucopetapi.service.appointment;

import com.uco.ucopetapi.domain.appointment.AppointmentDomain;
import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.dto.appointment.AppointmentStatusDTO;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeDTO;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.pets.PetDTO;
import com.uco.ucopetapi.dto.tutorPet.TutorPetDTO;
import com.uco.ucopetapi.event.AppointmentCreatedEvent;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.repository.appointment.IAppointmentRepository;
import com.uco.ucopetapi.service.appointmentType.AppointmentTypeService;
import com.uco.ucopetapi.service.doctor.DoctorService;
import com.uco.ucopetapi.service.person.PersonService;
import com.uco.ucopetapi.service.pet.PetService;
import com.uco.ucopetapi.service.tutorPet.TutorPetService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final ZoneId APPLICATION_ZONE_ID = ZoneId.systemDefault();
    private static final String PENDING_STATUS = "PENDING";
    private static final String CONFIRMED_STATUS = "CONFIRMED";
    private static final String CANCELLED_STATUS = "CANCELLED";
    private static final String COMPLETED_STATUS = "COMPLETED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(
            PENDING_STATUS,
            CONFIRMED_STATUS,
            CANCELLED_STATUS,
            COMPLETED_STATUS
    );

    private final IAppointmentRepository appointmentRepository;
    private final AppointmentTypeService appointmentTypeService;
    private final DoctorService doctorService;
    private final PetService petService;
    private final PersonService personService;
    private final TutorPetService tutorPetService;
    private final ApplicationEventPublisher eventPublisher;

    public AppointmentService(
            IAppointmentRepository appointmentRepository,
            AppointmentTypeService appointmentTypeService,
            DoctorService doctorService,
            PetService petService,
            PersonService personService,
            TutorPetService tutorPetService,
            ApplicationEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentTypeService = appointmentTypeService;
        this.doctorService = doctorService;
        this.petService = petService;
        this.personService = personService;
        this.tutorPetService = tutorPetService;
        this.eventPublisher = eventPublisher;
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
        return appointmentRepository.findByTutorIdAndStatus(tutorId, PENDING_STATUS).stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public AppointmentDTO create(AppointmentDTO appointmentDTO) {
        AppointmentDomain appointment = toDomain(appointmentDTO);
        validateAppointment(appointment, null);
        appointment.setId(UUID.randomUUID());
        AppointmentDTO dto = toDTO(appointmentRepository.save(appointment));
        publishAppointmentCreatedEvent(dto);
        return dto;
    }

    @Transactional
    public Optional<AppointmentDTO> update(UUID id, AppointmentDTO appointmentDTO) {
        if (!appointmentRepository.existsById(id)) {
            return Optional.empty();
        }

        AppointmentDomain appointment = toDomain(appointmentDTO);
        validateAppointment(appointment, id);
        appointment.setId(id);
        return Optional.of(toDTO(appointmentRepository.save(appointment)));
    }

    @Transactional
    public Optional<AppointmentDTO> updateStatus(UUID id, AppointmentStatusDTO appointmentStatusDTO) {
        if (appointmentStatusDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos del estado de la cita son obligatorios");
        }

        return appointmentRepository.findById(id)
                .map(appointment -> {
                    updateStatusFields(appointment, appointmentStatusDTO);
                    validateStatusRules(appointment, id);
                    return toDTO(appointmentRepository.save(appointment));
                });
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

        DoctorDomain doctor = doctorService.findById(doctorId);
        PersonDTO person = personService.findById(doctor.getIdPerson());
        return person.firstName() + " " + person.lastName();
    }

    private String findPetName(UUID petId) {
        if (petId == null) {
            return null;
        }

        return petService.getById(petId).getName();
    }

    private String findTutorName(UUID tutorId) {
        if (tutorId == null) {
            return null;
        }

        TutorPetDTO tutor = tutorPetService.findById(tutorId);
        PersonDTO person = personService.findById(tutor.getPerson());
        return person.firstName() + " " + person.lastName();
    }

    private String findAppointmentTypeName(UUID appointmentTypeId) {
        if (appointmentTypeId == null) {
            return null;
        }

        return appointmentTypeService.findById(appointmentTypeId)
                .map(AppointmentTypeDTO::getName)
                .orElse(null);
    }

    private void publishAppointmentCreatedEvent(AppointmentDTO appointmentDTO) {
        UUID tutorPersonId = tutorPetService.findById(appointmentDTO.getTutorId()).getPerson();
        eventPublisher.publishEvent(new AppointmentCreatedEvent(
                appointmentDTO.getId(),
                tutorPersonId,
                appointmentDTO.getPetName(),
                appointmentDTO.getAppointmentDate(),
                appointmentDTO.getAppointmentTime()
        ));
    }

    private void validateAppointment(AppointmentDomain appointment, UUID currentAppointmentId) {
        validateRequiredSchedule(appointment);
        validateStatusRules(appointment, currentAppointmentId);
        validateRelationships(appointment);
        validateDoctorScheduleAvailability(appointment, currentAppointmentId);
    }

    private void validateRequiredSchedule(AppointmentDomain appointment) {
        if (appointment.getAppointmentDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de la cita es obligatoria");
        }
        if (appointment.getAppointmentDate().isBefore(LocalDate.now(APPLICATION_ZONE_ID))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de la cita no puede estar en el pasado");
        }
        if (appointment.getAppointmentTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de la cita es obligatoria");
        }
    }

    private void validateStatusRules(AppointmentDomain appointment, UUID currentAppointmentId) {
        if (appointment.getStatus() == null || appointment.getStatus().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado de la cita es obligatorio");
        }

        String status = appointment.getStatus().trim().toUpperCase();
        appointment.setStatus(status);
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El estado de la cita solo puede ser PENDING, CONFIRMED, CANCELLED o COMPLETED");
        }

        if (CANCELLED_STATUS.equals(status) && isBlank(appointment.getCancellationReason())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La justificacion de cancelacion es obligatoria cuando la cita se cancela");
        }

        if (!CANCELLED_STATUS.equals(status) && !isBlank(appointment.getCancellationReason())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La justificacion de cancelacion solo debe enviarse cuando la cita esta cancelada");
        }

        if (CANCELLED_STATUS.equals(status)
                && currentAppointmentId != null
                && wasCompleted(currentAppointmentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se puede cancelar una cita que ya esta completada");
        }
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

        try {
            doctorService.findById(doctorId);
        } catch (BusinessException | NoSuchElementException _) {
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

        TutorPetDTO tutor = tutorPetService.findById(tutorId);
        PetDTO pet;
        try {
            pet = petService.getById(petId);
        } catch (NoSuchElementException _) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe una mascota con el id indicado");
        }

        if (!tutor.getId().equals(pet.getTutorId())) {
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
        if (!appointmentTypeService.isActive(appointmentTypeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El tipo de cita indicado no esta activo");
        }
    }

    private void validateDoctorScheduleAvailability(AppointmentDomain appointment, UUID currentAppointmentId) {
        boolean scheduleTaken = currentAppointmentId == null
                ? appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                        appointment.getDoctorId(),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime())
                : appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNot(
                        appointment.getDoctorId(),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime(),
                        currentAppointmentId);

        if (scheduleTaken) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El medico ya tiene una cita programada para esa fecha y hora");
        }
    }

    private boolean wasCompleted(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(AppointmentDomain::getStatus)
                .map(COMPLETED_STATUS::equals)
                .orElse(false);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void updateStatusFields(AppointmentDomain appointment, AppointmentStatusDTO appointmentStatusDTO) {
        appointment.setStatus(appointmentStatusDTO.getStatus());
        appointment.setCancellationReason(appointmentStatusDTO.getCancellationReason());
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
        appointmentDTO.setTutorName(findRelatedName(() -> findTutorName(appointment.getTutorId())));
        appointmentDTO.setPetId(appointment.getPetId());
        appointmentDTO.setPetName(findRelatedName(() -> findPetName(appointment.getPetId())));
        appointmentDTO.setDoctorId(appointment.getDoctorId());
        appointmentDTO.setDoctorName(findRelatedName(() -> findDoctorName(appointment.getDoctorId())));
        appointmentDTO.setAppointmentTypeId(appointment.getAppointmentTypeId());
        appointmentDTO.setAppointmentTypeName(findAppointmentTypeName(appointment.getAppointmentTypeId()));
        appointmentDTO.setAppointmentDate(appointment.getAppointmentDate());
        appointmentDTO.setAppointmentTime(appointment.getAppointmentTime());
        appointmentDTO.setStatus(appointment.getStatus());
        appointmentDTO.setCancellationReason(appointment.getCancellationReason());
        return appointmentDTO;
    }

    private String findRelatedName(Supplier<String> nameSupplier) {
        try {
            return nameSupplier.get();
        } catch (BusinessException | NoSuchElementException | ResponseStatusException _) {
            return null;
        }
    }
}