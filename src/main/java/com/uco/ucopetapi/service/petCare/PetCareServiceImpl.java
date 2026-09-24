package com.uco.ucopetapi.service.petCare;

import com.uco.ucopetapi.domain.petCare.PetCareDomain;
import com.uco.ucopetapi.domain.vitalSigns.VitalSignsDomain;
import com.uco.ucopetapi.dto.petCare.PetCareDto;
import com.uco.ucopetapi.dto.petCare.PetCareStatus;
import com.uco.ucopetapi.dto.vitalSigns.VitalSignsDTO;
import com.uco.ucopetapi.repository.petCare.PetCareRepository;
import com.uco.ucopetapi.service.attention.CurrentDoctorProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class PetCareServiceImpl implements PetCareService {

    private final PetCareRepository petCareRepository;
    private final CurrentDoctorProvider currentDoctorProvider;

    public PetCareServiceImpl(final PetCareRepository petCareRepository,
                              final CurrentDoctorProvider currentDoctorProvider) {
        this.petCareRepository = petCareRepository;
        this.currentDoctorProvider = currentDoctorProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetCareDto> findAll() {
        return petCareRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PetCareDto findById(final UUID id) {
        return petCareRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NoSuchElementException("Atención veterinaria no encontrada: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PetCareDto> findByFilter(final UUID episodeId, final PetCareStatus petCareStatus,
                                         final String description) {
        String normalizedDescription = blankToNull(description);
        List<PetCareDomain> petCares;

        if (episodeId != null && petCareStatus == null && normalizedDescription == null) {
            petCares = petCareRepository.findByEpisodeId(episodeId);
        } else if (episodeId == null && petCareStatus != null && normalizedDescription == null) {
            petCares = petCareRepository.findByPetCareStatus(petCareStatus);
        } else if (episodeId == null && petCareStatus == null && normalizedDescription != null) {
            petCares = petCareRepository.findByDescriptionContainingIgnoreCase(normalizedDescription);
        } else if (episodeId == null && petCareStatus == null) {
            petCares = petCareRepository.findAll();
        } else {
            petCares = petCareRepository.findAll().stream()
                    .filter(petCare -> episodeId == null || episodeId.equals(petCare.getEpisodeId()))
                    .filter(petCare -> petCareStatus == null || petCareStatus == petCare.getPetCareStatus())
                    .filter(petCare -> normalizedDescription == null
                            || (petCare.getDescription() != null
                            && petCare.getDescription().toLowerCase().contains(normalizedDescription.toLowerCase())))
                    .toList();
        }

        return petCares.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PetCareDto create(final PetCareDto request) {
        request.setDoctorId(currentDoctorProvider.get().getDoctorId());
        UUID id = request.getId() != null ? request.getId() : UUID.randomUUID();
        PetCareDomain petCare = toEntity(request, id);
        applyVitalSigns(petCare, request.getVitalSigns());
        petCare.validate();
        return toDto(petCareRepository.save(petCare));
    }

    @Override
    @Transactional
    public PetCareDto update(final UUID id, final PetCareDto request) {
        PetCareDomain petCare = petCareRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Atención veterinaria no encontrada: " + id));

        if (request.getEpisodeId() != null) {
            petCare.setEpisodeId(request.getEpisodeId());
        }
        if (request.getDoctorId() != null) {
            petCare.setDoctorId(request.getDoctorId());
        }
        if (request.getAttentionDate() != null) {
            petCare.setAttentionDate(request.getAttentionDate());
        }
        if (request.getDescription() != null) {
            petCare.setDescription(request.getDescription());
        }
        if (request.getPetCareStatus() != null) {
            petCare.setPetCareStatus(request.getPetCareStatus());
        }
        if (request.getVitalSigns() != null) {
            applyVitalSigns(petCare, request.getVitalSigns());
        }

        petCare.validate();
        return toDto(petCareRepository.save(petCare));
    }

    @Override
    @Transactional
    public PetCareDto complete(final UUID petCareId, final UUID requestingDoctorId, final String summary) {
        PetCareDomain petCare = petCareRepository.findById(petCareId)
                .orElseThrow(() -> new NoSuchElementException("Atención veterinaria no encontrada: " + petCareId));
        petCare.complete(requestingDoctorId, summary);
        petCare.validate();
        return toDto(petCareRepository.save(petCare));
    }

    private PetCareDto toDto(final PetCareDomain petCare) {
        List<VitalSignsDTO> vitalSigns = petCare.getVitalSigns() == null
                ? List.of()
                : petCare.getVitalSigns().stream()
                .map(this::toVitalSignsDto)
                .toList();

        PetCareDto dto = new PetCareDto(
                petCare.getId(),
                petCare.getEpisodeId(),
                petCare.getDoctorId(),
                petCare.getAttentionDate(),
                petCare.getDescription(),
                petCare.getPetCareStatus(),
                vitalSigns
        );
        dto.setAppointmentId(petCare.getAppointmentId());
        return dto;
    }

    private PetCareDomain toEntity(final PetCareDto request, final UUID id) {
        PetCareDomain petCare = new PetCareDomain(
                id,
                request.getEpisodeId(),
                request.getDoctorId(),
                request.getAttentionDate() != null ? request.getAttentionDate() : LocalDateTime.now(),
                request.getDescription() != null ? request.getDescription() : "Atención sin descripción",
                request.getPetCareStatus() != null ? request.getPetCareStatus() : PetCareStatus.REGISTERED
        );
        petCare.setAppointmentId(request.getAppointmentId());
        return petCare;
    }

    private VitalSignsDTO toVitalSignsDto(final VitalSignsDomain vitalSigns) {
        return new VitalSignsDTO(
                vitalSigns.getId(),
                vitalSigns.getTemperature(),
                vitalSigns.getHeartRate(),
                vitalSigns.getRespiratoryRate(),
                vitalSigns.getSystolicPressure(),
                vitalSigns.getDiastolicPressure(),
                vitalSigns.getWeight(),
                vitalSigns.getBodyConditionScore(),
                vitalSigns.getMeasurementDate()
        );
    }

    private void applyVitalSigns(final PetCareDomain petCare, final List<VitalSignsDTO> vitalSigns) {
        if (vitalSigns == null || vitalSigns.isEmpty()) {
            return;
        }
        for (VitalSignsDTO dto : vitalSigns) {
            if (dto.getId() == null) {
                petCare.addVitalSign(toVitalSignsEntity(dto, UUID.randomUUID()));
                continue;
            }
            VitalSignsDomain existing = petCare.getVitalSigns().stream()
                    .filter(vitalSign -> dto.getId().equals(vitalSign.getId()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException(
                            "Signos vitales no encontrados en la atención: " + dto.getId()));
            updateVitalSigns(existing, dto);
        }
    }

    private void updateVitalSigns(final VitalSignsDomain vitalSigns, final VitalSignsDTO dto) {
        if (dto.getTemperature() != null) {
            vitalSigns.setTemperature(dto.getTemperature());
        }
        if (dto.getHeartRate() != null) {
            vitalSigns.setHeartRate(dto.getHeartRate());
        }
        if (dto.getRespiratoryRate() != null) {
            vitalSigns.setRespiratoryRate(dto.getRespiratoryRate());
        }
        if (dto.getSystolicPressure() != null) {
            vitalSigns.setSystolicPressure(dto.getSystolicPressure());
        }
        if (dto.getDiastolicPressure() != null) {
            vitalSigns.setDiastolicPressure(dto.getDiastolicPressure());
        }
        if (dto.getWeight() != null) {
            vitalSigns.setWeight(dto.getWeight());
        }
        if (dto.getBodyConditionScore() != null) {
            vitalSigns.setBodyConditionScore(dto.getBodyConditionScore());
        }
        if (dto.getMeasurementDate() != null) {
            vitalSigns.setMeasurementDate(dto.getMeasurementDate());
        }
    }

    private VitalSignsDomain toVitalSignsEntity(final VitalSignsDTO dto, final UUID id) {
        return new VitalSignsDomain(
                id,
                null,
                dto.getTemperature(),
                dto.getHeartRate(),
                dto.getRespiratoryRate(),
                dto.getSystolicPressure(),
                dto.getDiastolicPressure(),
                dto.getWeight(),
                dto.getBodyConditionScore(),
                dto.getMeasurementDate() != null ? dto.getMeasurementDate() : LocalDateTime.now()
        );
    }

    private String blankToNull(final String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}