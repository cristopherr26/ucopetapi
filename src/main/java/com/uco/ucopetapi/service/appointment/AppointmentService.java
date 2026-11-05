package com.uco.ucopetapi.service.appointment;

import com.uco.ucopetapi.domain.appointment.AppointmentDomain;
import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.dto.appointment.AppointmentStatusDTO;
import com.uco.ucopetapi.dto.appointmentType.AppointmentTypeDTO;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.dto.pet.PetDTO;
import com.uco.ucopetapi.dto.tutorPet.TutorPetDTO;
import com.uco.ucopetapi.event.AppointmentCreatedEvent;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.messaging.appointment.AppointmentCreationSenderBroker;
import com.uco.ucopetapi.messaging.appointment.AppointmentDeactivationSenderBroker;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final ZoneId APPLICATION_ZONE_ID = ZoneId.systemDefault();
    private static final int CANCELLATION_REASON_MAX_LENGTH = 255;
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
    private final AppointmentCreationSenderBroker creationSender;
    private final AppointmentDeactivationSenderBroker statusSender;

    public AppointmentService(
            IAppointmentRepository appointmentRepository,
            AppointmentTypeService appointmentTypeService,
            DoctorService doctorService,
            PetService petService,
            PersonService personService,
            TutorPetService tutorPetService,
            ApplicationEventPublisher eventPublisher,
            AppointmentCreationSenderBroker creationSender,
            AppointmentDeactivationSenderBroker statusSender) {
        this.appointmentRepository = appointmentRepository;
        this.appointmentTypeService = appointmentTypeService;
        this.doctorService = doctorService;
        this.petService = petService;
        this.personService = personService;
        this.tutorPetService = tutorPetService;
        this.eventPublisher = eventPublisher;
        this.creationSender = creationSender;
        this.statusSender = statusSender;
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

    /**
     * Valida la cita y la PUBLICA en RabbitMQ; quien la guarda en la BD es el procesador de salud.
     * El id de la cita se genera aqui y se deja en appointmentDTO para poder devolverlo al cliente.
     *
     * @return el idMessage con el que viaja el mensaje
     */
    @Transactional
    public String create(AppointmentDTO appointmentDTO) {
        AppointmentDomain appointment = toDomain(appointmentDTO);
        validateAppointment(appointment, null, null);
        appointment.setId(UUID.randomUUID());
        appointmentDTO.setId(appointment.getId());
        // Primero el evento (comparte transaccion): si publicar el mensaje falla, todo se revierte.
        publishAppointmentCreatedEvent(appointment);
        return publishSave(appointment);
    }

    /**
     * Valida los cambios y los PUBLICA en RabbitMQ; quien los guarda es el procesador de salud.
     *
     * @return el idMessage con el que viaja el mensaje, o vacio si la cita no existe
     */
    @Transactional
    public Optional<String> update(UUID id, AppointmentDTO appointmentDTO) {
        Optional<AppointmentDomain> stored = appointmentRepository.findById(id);
        if (stored.isEmpty()) {
            return Optional.empty();
        }

        AppointmentDomain appointment = toDomain(appointmentDTO);
        validateAppointment(appointment, id, stored.get());
        appointment.setId(id);
        return Optional.of(publishSave(appointment));
    }

    /**
     * Valida un cambio de estado y lo PUBLICA en RabbitMQ (cola de "desactivar"); el procesador de
     * salud lo aplica. Es la version asincrona usada por el endpoint PATCH de citas.
     *
     * @return el idMessage con el que viaja el mensaje, o vacio si la cita no existe
     */
    @Transactional
    public Optional<String> requestStatusChange(UUID id, AppointmentStatusDTO appointmentStatusDTO) {
        if (appointmentStatusDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos del estado de la cita son obligatorios");
        }
        Optional<AppointmentDomain> current = appointmentRepository.findById(id);
        if (current.isEmpty()) {
            return Optional.empty();
        }

        // Se valida sobre una copia: la cita real NO se toca aqui, la modifica el procesador.
        AppointmentDomain candidate = new AppointmentDomain();
        candidate.setId(id);
        updateStatusFields(candidate, appointmentStatusDTO);
        validateStatusRules(candidate, id);
        validateScheduleWhenReactivated(current.get(), current.get().getStatus(), candidate.getStatus());
        return Optional.of(publishStatus(candidate));
    }

    /**
     * Cambio de estado SINCRONO. Lo usa el flujo interno de atencion (AppointmentAttentionOrchestrator)
     * porque necesita completar la cita dentro de su misma transaccion y leer el resultado.
     * Los clientes HTTP usan requestStatusChange (asincrono).
     */
    @Transactional
    public Optional<AppointmentDTO> updateStatus(UUID id, AppointmentStatusDTO appointmentStatusDTO) {
        if (appointmentStatusDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Los datos del estado de la cita son obligatorios");
        }

        return appointmentRepository.findById(id)
                .map(appointment -> {
                    String previousStatus = appointment.getStatus();
                    updateStatusFields(appointment, appointmentStatusDTO);
                    validateStatusRules(appointment, id);
                    validateScheduleWhenReactivated(appointment, previousStatus, appointment.getStatus());
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

    private String publishSave(AppointmentDomain appointment) {
        String idMessage = UUID.randomUUID().toString();
        creationSender.execute(appointment, idMessage);
        return idMessage;
    }

    private String publishStatus(AppointmentDomain appointment) {
        String idMessage = UUID.randomUUID().toString();
        // Solo viajan id, estado y justificacion; el resto de campos queda en null
        AppointmentDomain message = new AppointmentDomain();
        message.setId(appointment.getId());
        message.setStatus(appointment.getStatus());
        message.setCancellationReason(appointment.getCancellationReason());
        statusSender.execute(message, idMessage);
        return idMessage;
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

    private void publishAppointmentCreatedEvent(AppointmentDomain appointment) {
        UUID tutorPersonId = tutorPetService.findById(appointment.getTutorId()).getPerson();
        eventPublisher.publishEvent(new AppointmentCreatedEvent(
                appointment.getId(),
                tutorPersonId,
                findPetName(appointment.getPetId()),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        ));
    }

    // "stored" es la cita tal como esta guardada (null al crear): solo se usa para saber si cambio el horario.
    private void validateAppointment(AppointmentDomain appointment, UUID currentAppointmentId,
                                     AppointmentDomain stored) {
        validateRequiredSchedule(appointment, stored);
        validateStatusRules(appointment, currentAppointmentId);
        validateRelationships(appointment);
        // Una cita cancelada no ocupa el horario del medico, asi que no compite por el.
        if (!CANCELLED_STATUS.equals(appointment.getStatus())) {
            validateDoctorScheduleAvailability(appointment, currentAppointmentId);
        }
    }

    // Si la cita estaba cancelada su horario pudo ser tomado por otra: al reactivarla se revisa de nuevo.
    private void validateScheduleWhenReactivated(AppointmentDomain appointment, String previousStatus,
                                                 String newStatus) {
        if (CANCELLED_STATUS.equals(previousStatus) && !CANCELLED_STATUS.equals(newStatus)) {
            validateDoctorScheduleAvailability(appointment, appointment.getId());
        }
    }

    // La fecha y la hora son obligatorias. La regla "no puede estar en el pasado" se aplica al crear y
    // cuando una edicion cambia la fecha o la hora; si el horario queda igual no se vuelve a exigir, asi se
    // puede corregir otro dato de una cita de hoy cuya hora ya paso. Se compara al minuto: la hora de este
    // mismo minuto todavia se acepta.
    private void validateRequiredSchedule(AppointmentDomain appointment, AppointmentDomain stored) {
        if (appointment.getAppointmentDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de la cita es obligatoria");
        }
        if (appointment.getAppointmentTime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de la cita es obligatoria");
        }
        if (stored != null && hasSameSchedule(appointment, stored)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(APPLICATION_ZONE_ID).truncatedTo(ChronoUnit.MINUTES);
        if (appointment.getAppointmentDate().isBefore(now.toLocalDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de la cita no puede estar en el pasado");
        }
        if (LocalDateTime.of(appointment.getAppointmentDate(), appointment.getAppointmentTime()).isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La hora de la cita no puede estar en el pasado");
        }
    }

    private boolean hasSameSchedule(AppointmentDomain appointment, AppointmentDomain stored) {
        LocalTime requestedTime = appointment.getAppointmentTime().truncatedTo(ChronoUnit.MINUTES);
        LocalTime storedTime = stored.getAppointmentTime() == null
                ? null
                : stored.getAppointmentTime().truncatedTo(ChronoUnit.MINUTES);
        return appointment.getAppointmentDate().equals(stored.getAppointmentDate())
                && requestedTime.equals(storedTime);
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

        if (!isBlank(appointment.getCancellationReason())
                && appointment.getCancellationReason().trim().length() > CANCELLATION_REASON_MAX_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La justificacion de cancelacion no puede superar " + CANCELLATION_REASON_MAX_LENGTH
                            + " caracteres");
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

        // TutorPetService y PetService lanzan BusinessException cuando no encuentran el registro
        TutorPetDTO tutor;
        try {
            tutor = tutorPetService.findById(tutorId);
        } catch (BusinessException | NoSuchElementException _) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No existe un tutor con el id indicado");
        }
        PetDTO pet;
        try {
            pet = petService.getById(petId);
        } catch (BusinessException | NoSuchElementException _) {
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
                ? appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                        appointment.getDoctorId(),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime(),
                        CANCELLED_STATUS)
                : appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndIdNotAndStatusNot(
                        appointment.getDoctorId(),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime(),
                        currentAppointmentId,
                        CANCELLED_STATUS);

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
