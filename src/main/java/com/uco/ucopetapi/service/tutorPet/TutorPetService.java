package com.uco.ucopetapi.service.tutorPet;

import com.uco.ucopetapi.domain.tutorPet.TutorPetDomain;
import com.uco.ucopetapi.dto.tutorPet.TutorPetDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.messaging.tutorPet.TutorPetCreationSenderBroker;
import com.uco.ucopetapi.messaging.tutorPet.TutorPetDeactivationSenderBroker;
import com.uco.ucopetapi.messaging.tutorPet.TutorPetUpdateSenderBroker;
import com.uco.ucopetapi.repository.tutorPet.ITutorPetRepository;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class TutorPetService {

    private final ITutorPetRepository tutorPetRepository;
    private final TutorPetCreationSenderBroker creationSender;
    private final TutorPetUpdateSenderBroker updateSender;
    private final TutorPetDeactivationSenderBroker deactivationSender;

    public TutorPetService(ITutorPetRepository tutorPetRepository,
                           TutorPetCreationSenderBroker creationSender,
                           TutorPetUpdateSenderBroker updateSender,
                           TutorPetDeactivationSenderBroker deactivationSender) {
        this.tutorPetRepository = tutorPetRepository;
        this.creationSender = creationSender;
        this.updateSender = updateSender;
        this.deactivationSender = deactivationSender;
    }

    public List<TutorPetDTO> findAll() {
        return tutorPetRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public TutorPetDTO findById(UUID id) {
        TutorPetDomain tutorPet = getOrThrow(id);
        return toDTO(tutorPet);
    }

    public List<TutorPetDTO> findByPersonId(UUID personId) {
        return tutorPetRepository.findByPerson_Id(personId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public TutorPetDTO create(TutorPetDTO tutorPetDTO) {
        if (tutorPetDTO == null || tutorPetDTO.getPerson() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La persona asociada al TutorPet es obligatoria.");
        }

        TutorPetDTO message = new TutorPetDTO(UUID.randomUUID(), tutorPetDTO.getPerson());
        creationSender.execute(message, UUID.randomUUID().toString());
        return message;
    }

    public TutorPetDTO update(UUID id, TutorPetDTO tutorPetDTO) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El identificador de TutorPet es obligatorio.");
        }
        if (tutorPetDTO == null || tutorPetDTO.getPerson() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La persona asociada al TutorPet es obligatoria.");
        }

        TutorPetDTO message = new TutorPetDTO(id, tutorPetDTO.getPerson());
        updateSender.execute(message, UUID.randomUUID().toString());
        return message;
    }

    public void deactivate(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El identificador de TutorPet es obligatorio.");
        }
        TutorPetDTO message = new TutorPetDTO();
        message.setId(id);
        deactivationSender.execute(message, UUID.randomUUID().toString());
    }

    private TutorPetDomain getOrThrow(UUID id) {
        return tutorPetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "TutorPet no encontrado con id: " + id));
    }

    private TutorPetDTO toDTO(TutorPetDomain tutorPet) {
        return new TutorPetDTO(tutorPet.getId(), tutorPet.getPerson().getId());
    }
}