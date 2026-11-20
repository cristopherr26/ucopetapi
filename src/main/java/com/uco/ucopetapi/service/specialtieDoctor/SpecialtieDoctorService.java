package com.uco.ucopetapi.service.specialtieDoctor;

import com.uco.ucopetapi.domain.specialtie.SpecialtieDomain;
import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.domain.specialtieDoctor.VerificationStatus;
import com.uco.ucopetapi.domain.specialtieDoctor.mapper.SpecialtieDoctorMapper;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.messaging.specialtieDoctor.MessageSenderBrokerSpecialtieDoctorCreate;
import com.uco.ucopetapi.messaging.specialtieDoctor.MessageSenderBrokerSpecialtieDoctorDeactivate;
import com.uco.ucopetapi.messaging.specialtieDoctor.MessageSenderBrokerSpecialtieDoctorUpdate;
import com.uco.ucopetapi.repository.specialtie.ISpecialtieRepository;
import com.uco.ucopetapi.repository.specialtieDoctor.ISpecialtieDoctorRepository;
import com.uco.ucopetapi.repository.specialtieDoctor.SpecialtieDoctorSpecifications;
import com.uco.ucopetapi.service.specialtie.exception.SpecialtieConflictException;
import com.uco.ucopetapi.service.specialtie.exception.SpecialtieNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class SpecialtieDoctorService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");
    private static final String DUPLICATE_MESSAGE = "Este doctor ya tiene registrada esa especialidad.";
    private static final String INVALID_URL_MESSAGE =
            "La URL del certificado (certificateUrl) debe ser una URL http o https válida.";

    private final ISpecialtieDoctorRepository specialtieDoctorRepository;
    private final ISpecialtieRepository specialtieRepository;
    private final SpecialtieDoctorMapper specialtieDoctorMapper;
    private final MessageSenderBrokerSpecialtieDoctorCreate specialtieDoctorCreateSenderBroker;
    private final MessageSenderBrokerSpecialtieDoctorUpdate specialtieDoctorUpdateSenderBroker;
    private final MessageSenderBrokerSpecialtieDoctorDeactivate specialtieDoctorDeactivateSenderBroker;

    public SpecialtieDoctorService(ISpecialtieDoctorRepository specialtieDoctorRepository,
                                   ISpecialtieRepository specialtieRepository,
                                   SpecialtieDoctorMapper specialtieDoctorMapper,
                                   MessageSenderBrokerSpecialtieDoctorCreate specialtieDoctorCreateSenderBroker,
                                   MessageSenderBrokerSpecialtieDoctorUpdate specialtieDoctorUpdateSenderBroker,
                                   MessageSenderBrokerSpecialtieDoctorDeactivate specialtieDoctorDeactivateSenderBroker) {
        this.specialtieDoctorRepository = specialtieDoctorRepository;
        this.specialtieRepository = specialtieRepository;
        this.specialtieDoctorMapper = specialtieDoctorMapper;
        this.specialtieDoctorCreateSenderBroker = specialtieDoctorCreateSenderBroker;
        this.specialtieDoctorUpdateSenderBroker = specialtieDoctorUpdateSenderBroker;
        this.specialtieDoctorDeactivateSenderBroker = specialtieDoctorDeactivateSenderBroker;
    }

    public List<SpecialtieDoctorDomain> findAll(UUID headquarterId) {
        validateHeadquarterId(headquarterId);
        return specialtieDoctorRepository.findByHeadquarterId(headquarterId);
    }

    public SpecialtieDoctorDomain findById(UUID id) {
        return specialtieDoctorRepository.findById(id)
                .orElseThrow(() -> new SpecialtieNotFoundException(
                        "No se encontro una relacion doctor-especialidad con id " + id));
    }

    public List<SpecialtieDoctorDomain> findByFilter(UUID headquarterId, UUID idDoctor, UUID idSpecialtie,
                                                     VerificationStatus status) {
        validateHeadquarterId(headquarterId);

        // El status null en BD se trata como PENDING en la consulta (coalesce).
        return specialtieDoctorRepository.findAll(
                SpecialtieDoctorSpecifications.filter(headquarterId, idDoctor, idSpecialtie, status, false));
    }

    public List<SpecialtieDoctorDomain> findVerified(UUID headquarterId, UUID idSpecialtie) {
        validateHeadquarterId(headquarterId);
        if (idSpecialtie == null) {
            throw new BusinessException("El id de la especialidad es obligatorio.");
        }

        boolean specialtieActive = specialtieRepository.findById(idSpecialtie)
                .map(SpecialtieDomain::isActive)
                .orElse(false);
        if (!specialtieActive) {
            return List.of();
        }

        return specialtieDoctorRepository.findAll(SpecialtieDoctorSpecifications.filter(
                headquarterId, null, idSpecialtie, VerificationStatus.VERIFIED, true));
    }

    @Transactional
    public SpecialtieDoctorDomain createNewSpecialtieDoctor(SpecialtieDoctorDomain specialtieDoctor) {
        validateCommonFields(specialtieDoctor);
        String certificateUrl = validateCertificateUrl(specialtieDoctor.getCertificateUrl());
        String certificateNotes = normalizeOptionalText(specialtieDoctor.getCertificateNotes(), "certificateNotes");
        validateSpecialtieIsActive(specialtieDoctor.getIdSpecialtie());

        List<SpecialtieDoctorDomain> existingAssignments = specialtieDoctorRepository.findByDoctor_IdAndIdSpecialtie(
                specialtieDoctor.getDoctor().getId(), specialtieDoctor.getIdSpecialtie());

        // Ya existe activa y no rechazada (PENDING o VERIFIED): sigue siendo 409.
        boolean hasLiveAssignment = existingAssignments.stream()
                .anyMatch(sd -> sd.isActive() && sd.getStatus() != VerificationStatus.REJECTED);
        if (hasLiveAssignment) {
            throw new SpecialtieConflictException(DUPLICATE_MESSAGE);
        }

        // Existe pero está inactiva o REJECTED: se reactiva en PENDING con el nuevo certificado.
        if (!existingAssignments.isEmpty()) {
            SpecialtieDoctorDomain reused = existingAssignments.getFirst();
            reused.setDoctor(specialtieDoctor.getDoctor());
            reused.setHeadquarterId(specialtieDoctor.getHeadquarterId());
            reused.setCertificateUrl(certificateUrl);
            reused.setCertificateNotes(certificateNotes);
            resetToPending(reused);
            reused.setActive(Boolean.TRUE);

            SpecialtieDoctorDomain saved = specialtieDoctorRepository.saveAndFlush(reused);
            // Es una fila existente (mismo id): se notifica por la cola de update, no por la de create.
            specialtieDoctorUpdateSenderBroker.execute(specialtieDoctorMapper.toDTO(saved), newMessageId());
            return saved;
        }

        specialtieDoctor.setId(null);
        specialtieDoctor.setCertificateUrl(certificateUrl);
        specialtieDoctor.setCertificateNotes(certificateNotes);
        resetToPending(specialtieDoctor);
        specialtieDoctor.setActive(Boolean.TRUE);

        SpecialtieDoctorDomain saved = specialtieDoctorRepository.saveAndFlush(specialtieDoctor);

        specialtieDoctorCreateSenderBroker.execute(specialtieDoctorMapper.toDTO(saved), newMessageId());

        return saved;
    }

    @Transactional
    public SpecialtieDoctorDomain updateSpecialtieDoctor(UUID id, SpecialtieDoctorDomain specialtieDoctor) {
        SpecialtieDoctorDomain existingSpecialtieDoctor = findById(id);

        validateFieldsForUpdate(existingSpecialtieDoctor, specialtieDoctor);

        // certificateUrl y certificateNotes son opcionales en la actualización: si no vienen, se conservan.
        String newCertificateUrl = specialtieDoctor.getCertificateUrl() != null
                ? validateCertificateUrl(specialtieDoctor.getCertificateUrl())
                : null;
        boolean certificateChanged = newCertificateUrl != null
                && !newCertificateUrl.equals(existingSpecialtieDoctor.getCertificateUrl());

        existingSpecialtieDoctor.setDoctor(specialtieDoctor.getDoctor());
        existingSpecialtieDoctor.setIdSpecialtie(specialtieDoctor.getIdSpecialtie());
        existingSpecialtieDoctor.setHeadquarterId(specialtieDoctor.getHeadquarterId());

        if (newCertificateUrl != null) {
            existingSpecialtieDoctor.setCertificateUrl(newCertificateUrl);
        }
        if (specialtieDoctor.getCertificateNotes() != null) {
            existingSpecialtieDoctor.setCertificateNotes(
                    normalizeOptionalText(specialtieDoctor.getCertificateNotes(), "certificateNotes"));
        }

        // Un certificado nuevo en una asignación VERIFIED o REJECTED la devuelve a PENDING para revisarla de nuevo.
        if (certificateChanged && existingSpecialtieDoctor.getStatus() != VerificationStatus.PENDING) {
            resetToPending(existingSpecialtieDoctor);
        }

        SpecialtieDoctorDomain saved = specialtieDoctorRepository.saveAndFlush(existingSpecialtieDoctor);

        specialtieDoctorUpdateSenderBroker.execute(specialtieDoctorMapper.toDTO(saved), newMessageId());

        return saved;
    }

    @Transactional
    public void deactivateSpecialtieDoctor(UUID id) {
        SpecialtieDoctorDomain specialtieDoctor = findById(id);

        specialtieDoctor.setActive(Boolean.FALSE);
        SpecialtieDoctorDomain saved = specialtieDoctorRepository.saveAndFlush(specialtieDoctor);

        specialtieDoctorDeactivateSenderBroker.execute(specialtieDoctorMapper.toDTO(saved), newMessageId());
    }

    @Transactional
    public SpecialtieDoctorDomain verifySpecialtieDoctor(UUID id) {
        SpecialtieDoctorDomain specialtieDoctor = findById(id);

        VerificationStatus currentStatus = specialtieDoctor.getStatus();
        if (currentStatus == VerificationStatus.VERIFIED) {
            throw new SpecialtieConflictException("La asignación ya está verificada.");
        }
        if (currentStatus != VerificationStatus.PENDING) {
            throw new SpecialtieConflictException(
                    "Solo se pueden verificar asignaciones en estado PENDING. Estado actual: " + currentStatus);
        }
        if (!specialtieDoctor.isActive()) {
            throw new BusinessException("No se puede verificar una asignación inactiva.");
        }
        if (specialtieDoctor.getCertificateUrl() == null || specialtieDoctor.getCertificateUrl().isBlank()) {
            throw new BusinessException("La asignación no tiene certificado (certificateUrl) para verificar.");
        }
        validateSpecialtieIsActive(specialtieDoctor.getIdSpecialtie());

        specialtieDoctor.setStatus(VerificationStatus.VERIFIED);
        specialtieDoctor.setVerifiedAt(LocalDateTime.now(ZONE_ID));
        specialtieDoctor.setVerifiedBy(currentPersonId());
        specialtieDoctor.setRejectionReason(null);

        return saveAndPublishUpdate(specialtieDoctor);
    }

    @Transactional
    public SpecialtieDoctorDomain rejectSpecialtieDoctor(UUID id, String reason) {
        SpecialtieDoctorDomain specialtieDoctor = findById(id);

        if (reason == null || reason.isBlank()) {
            throw new BusinessException("El motivo del rechazo (reason) es obligatorio.");
        }
        String trimmedReason = reason.trim();
        if (trimmedReason.length() > SpecialtieDoctorDomain.TEXT_MAX_LENGTH) {
            throw new BusinessException("El motivo del rechazo no puede superar "
                    + SpecialtieDoctorDomain.TEXT_MAX_LENGTH + " caracteres.");
        }
        if (specialtieDoctor.getStatus() == VerificationStatus.REJECTED) {
            throw new SpecialtieConflictException("La asignación ya está rechazada.");
        }

        specialtieDoctor.setStatus(VerificationStatus.REJECTED);
        specialtieDoctor.setRejectionReason(trimmedReason);
        specialtieDoctor.setVerifiedAt(LocalDateTime.now(ZONE_ID));
        specialtieDoctor.setVerifiedBy(currentPersonId());

        return saveAndPublishUpdate(specialtieDoctor);
    }

    /** Deja la asignación en PENDING y limpia todos los campos de verificación y rechazo. */
    private void resetToPending(SpecialtieDoctorDomain specialtieDoctor) {
        specialtieDoctor.setStatus(VerificationStatus.PENDING);
        specialtieDoctor.setVerifiedBy(null);
        specialtieDoctor.setVerifiedAt(null);
        specialtieDoctor.setRejectionReason(null);
    }

    /** Verificar y rechazar publican por la cola de update; el mensaje (DTO) incluye el status. */
    private SpecialtieDoctorDomain saveAndPublishUpdate(SpecialtieDoctorDomain specialtieDoctor) {
        SpecialtieDoctorDomain saved = specialtieDoctorRepository.saveAndFlush(specialtieDoctor);
        specialtieDoctorUpdateSenderBroker.execute(specialtieDoctorMapper.toDTO(saved), newMessageId());
        return saved;
    }

    private void validateFieldsForUpdate(SpecialtieDoctorDomain existingSpecialtieDoctor, SpecialtieDoctorDomain specialtieDoctor) {
        validateCommonFields(specialtieDoctor);

        UUID newDoctorId = specialtieDoctor.getDoctor().getId();
        UUID newSpecialtieId = specialtieDoctor.getIdSpecialtie();

        boolean doctorChanged = !newDoctorId.equals(existingSpecialtieDoctor.getDoctor().getId());
        boolean specialtieChanged = !newSpecialtieId.equals(existingSpecialtieDoctor.getIdSpecialtie());

        if (specialtieChanged) {
            validateSpecialtieIsActive(newSpecialtieId);
        }

        if ((doctorChanged || specialtieChanged)
                && specialtieDoctorRepository.existsByDoctor_IdAndIdSpecialtie(newDoctorId, newSpecialtieId)) {
            throw new SpecialtieConflictException(DUPLICATE_MESSAGE);
        }
    }

    private void validateCommonFields(SpecialtieDoctorDomain specialtieDoctor) {
        if (specialtieDoctor == null) {
            throw new BusinessException("La informacion de la relacion doctor-especialidad no puede ser nula.");
        }
        if (specialtieDoctor.getDoctor() == null || specialtieDoctor.getDoctor().getId() == null) {
            throw new BusinessException("El id del doctor es obligatorio.");
        }
        if (specialtieDoctor.getIdSpecialtie() == null) {
            throw new BusinessException("El id de la especialidad es obligatorio.");
        }

        validateHeadquarterId(specialtieDoctor.getHeadquarterId());

        if (!specialtieDoctor.getHeadquarterId().equals(specialtieDoctor.getDoctor().getHeadquarterId())) {
            throw new BusinessException("El doctor no pertenece a la sede indicada.");
        }

        if (!specialtieRepository.existsById(specialtieDoctor.getIdSpecialtie())) {
            throw new BusinessException("La especialidad especificada no existe.");
        }
    }

    private void validateSpecialtieIsActive(UUID idSpecialtie) {
        SpecialtieDomain specialtie = specialtieRepository.findById(idSpecialtie)
                .orElseThrow(() -> new BusinessException("La especialidad especificada no existe."));
        if (!specialtie.isActive()) {
            throw new BusinessException("La especialidad especificada está inactiva.");
        }
    }

    /** Devuelve la URL recortada si es http/https válida con host; si no, lanza 400. */
    private String validateCertificateUrl(String certificateUrl) {
        if (certificateUrl == null || certificateUrl.isBlank()) {
            throw new BusinessException("La URL del certificado (certificateUrl) es obligatoria.");
        }
        String trimmed = certificateUrl.trim();
        if (trimmed.length() > SpecialtieDoctorDomain.CERTIFICATE_URL_MAX_LENGTH) {
            throw new BusinessException("La URL del certificado no puede superar "
                    + SpecialtieDoctorDomain.CERTIFICATE_URL_MAX_LENGTH + " caracteres.");
        }
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            boolean validScheme = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
            if (!validScheme || uri.getHost() == null || uri.getHost().isBlank()) {
                throw new BusinessException(INVALID_URL_MESSAGE);
            }
        } catch (URISyntaxException _) {
            throw new BusinessException(INVALID_URL_MESSAGE);
        }
        return trimmed;
    }

    /** Recorta un texto opcional; vacío pasa a null. Lanza 400 si supera el máximo. */
    private String normalizeOptionalText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > SpecialtieDoctorDomain.TEXT_MAX_LENGTH) {
            throw new BusinessException("El campo " + fieldName + " no puede superar "
                    + SpecialtieDoctorDomain.TEXT_MAX_LENGTH + " caracteres.");
        }
        return trimmed;
    }

    /** Id de la persona autenticada (subject del JWT). Devuelve null si no se puede resolver. */
    private UUID currentPersonId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return null;
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException _) {
            return null;
        }
    }

    private void validateHeadquarterId(UUID headquarterId) {
        if (headquarterId == null) {
            throw new BusinessException("El ID de la sede es obligatorio.");
        }
    }

    private String newMessageId() {
        return UUID.randomUUID().toString();
    }
}
