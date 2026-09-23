package com.uco.ucopetapi.service.attention;

import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.dto.appointment.AppointmentStatusDTO;
import com.uco.ucopetapi.dto.attention.AttentionResponse;
import com.uco.ucopetapi.dto.attention.FinishAttentionRequest;
import com.uco.ucopetapi.dto.attention.StartAttentionRequest;
import com.uco.ucopetapi.dto.episode.EpisodeDto;
import com.uco.ucopetapi.dto.episode.EpisodeStatus;
import com.uco.ucopetapi.dto.pet.PetDTO;
import com.uco.ucopetapi.dto.petCare.PetCareDto;
import com.uco.ucopetapi.dto.petCare.PetCareStatus;
import com.uco.ucopetapi.exception.clinical.ClinicalException;
import com.uco.ucopetapi.repository.episode.EpisodeRepository;
import com.uco.ucopetapi.repository.petCare.PetCareRepository;
import com.uco.ucopetapi.service.appointment.AppointmentService;
import com.uco.ucopetapi.service.episode.EpisodeService;
import com.uco.ucopetapi.service.pet.PetService;
import com.uco.ucopetapi.service.petCare.PetCareService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AppointmentAttentionOrchestrator {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");
    private static final Set<String> ATTENDABLE_APPOINTMENT_STATUSES = Set.of("PENDING", "CONFIRMED");
    private static final List<EpisodeStatus> OPEN_EPISODE_STATUSES = List.of(
            EpisodeStatus.ACTIVE,
            EpisodeStatus.IN_OBSERVATION,
            EpisodeStatus.HOSPITALIZED
    );

    private final CurrentDoctorProvider currentDoctorProvider;
    private final AppointmentService appointmentService;
    private final PetCareRepository petCareRepository;
    private final PetService petService;
    private final EpisodeRepository episodeRepository;
    private final EpisodeService episodeService;
    private final PetCareService petCareService;

    public AppointmentAttentionOrchestrator(final CurrentDoctorProvider currentDoctorProvider,
                                            final AppointmentService appointmentService,
                                            final PetCareRepository petCareRepository,
                                            final PetService petService,
                                            final EpisodeRepository episodeRepository,
                                            final EpisodeService episodeService,
                                            final PetCareService petCareService) {
        this.currentDoctorProvider = currentDoctorProvider;
        this.appointmentService = appointmentService;
        this.petCareRepository = petCareRepository;
        this.petService = petService;
        this.episodeRepository = episodeRepository;
        this.episodeService = episodeService;
        this.petCareService = petCareService;
    }

    @Transactional
    public AttentionResponse start(final UUID appointmentId, final StartAttentionRequest request) {
        if (request == null) {
            throw ClinicalException.badRequest("Los datos de la atención son obligatorios");
        }

        CurrentDoctor doctor = currentDoctorProvider.get();
        AppointmentDTO appointment = appointmentService.findById(appointmentId)
                .orElseThrow(() -> ClinicalException.notFound("No se encontró la cita indicada"));

        String status = appointment.getStatus() == null ? "" : appointment.getStatus().trim().toUpperCase();
        if (!ATTENDABLE_APPOINTMENT_STATUSES.contains(status)) {
            throw ClinicalException.conflict("La cita no se puede atender en su estado actual");
        }
        if (appointment.getDoctorId() == null || !appointment.getDoctorId().equals(doctor.getDoctorId())) {
            throw ClinicalException.forbidden("La cita está asignada a otro veterinario");
        }
        if (petCareRepository.existsByAppointmentId(appointmentId)) {
            throw ClinicalException.conflict("La cita ya tiene una atención registrada");
        }

        PetDTO pet = petService.getById(appointment.getPetId());
        if (Boolean.FALSE.equals(pet.isActive())) {
            throw ClinicalException.badRequest("La mascota no está activa");
        }

        EpisodeDto episode = episodeRepository
                .findFirstByPetAndEpisodeStatusInOrderByStartDateDesc(appointment.getPetId(), OPEN_EPISODE_STATUSES)
                .map(existing -> episodeService.findById(existing.getId()))
                .orElseGet(() -> createEpisode(appointment.getPetId(), request.getReason()));

        PetCareDto petCareRequest = new PetCareDto();
        petCareRequest.setEpisodeId(episode.getId());
        petCareRequest.setAppointmentId(appointmentId);
        petCareRequest.setDoctorId(doctor.getDoctorId());
        petCareRequest.setAttentionDate(LocalDateTime.now(ZONE_ID));
        petCareRequest.setDescription(request.getDescription());
        petCareRequest.setPetCareStatus(PetCareStatus.IN_PROGRESS);
        petCareRequest.setVitalSigns(request.getVitalSigns());

        PetCareDto petCare = petCareService.create(petCareRequest);
        return new AttentionResponse(appointmentId, appointment.getStatus(), episode, petCare);
    }

    @Transactional
    public AttentionResponse finish(final UUID petCareId, final FinishAttentionRequest request) {
        if (request == null) {
            throw ClinicalException.badRequest("Los datos de cierre son obligatorios");
        }

        CurrentDoctor doctor = currentDoctorProvider.get();
        PetCareDto petCare = petCareService.complete(petCareId, doctor.getDoctorId(), request.getSummary());

        String appointmentStatus = null;
        if (petCare.getAppointmentId() != null) {
            AppointmentStatusDTO status = new AppointmentStatusDTO();
            status.setStatus("COMPLETED");
            AppointmentDTO appointment = appointmentService.updateStatus(petCare.getAppointmentId(), status)
                    .orElseThrow(() -> ClinicalException.notFound("No se encontró la cita indicada"));
            appointmentStatus = appointment.getStatus();
        }

        EpisodeDto episode = request.isCloseEpisode()
                ? episodeService.discharge(petCare.getEpisodeId(), request.getDischargeType(), request.getDischargeNotes())
                : episodeService.findById(petCare.getEpisodeId());

        return new AttentionResponse(petCare.getAppointmentId(), appointmentStatus, episode, petCare);
    }

    private EpisodeDto createEpisode(final UUID petId, final String reason) {
        EpisodeDto episode = new EpisodeDto();
        episode.setPet(petId);
        episode.setDescription(reason);
        episode.setStartDate(LocalDateTime.now(ZONE_ID));
        episode.setEpisodeStatus(EpisodeStatus.ACTIVE);
        return episodeService.create(episode);
    }
}
