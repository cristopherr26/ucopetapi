package com.uco.ucopetapi.service.tutorPet;

import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.domain.tutorPet.TutorPetDomain;
import com.uco.ucopetapi.dto.tutorPet.TutorPetDTO;
import com.uco.ucopetapi.repository.person.PersonRepository;
import com.uco.ucopetapi.repository.tutorPet.ITutorPetRepository;
import com.uco.ucopetapi.service.person.PersonService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class TutorPetService {

    private final ITutorPetRepository tutorPetRepository;
    private final PersonRepository personRepository;
    private final PersonService personService;

    public TutorPetService(ITutorPetRepository tutorPetRepository,
                           PersonRepository personRepository,
                           PersonService personService) {
        this.tutorPetRepository = tutorPetRepository;
        this.personRepository = personRepository;
        this.personService = personService;
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
        PersonDomain person = getPersonOrThrow(tutorPetDTO.getPerson());

        TutorPetDomain tutorPet = new TutorPetDomain();
        tutorPet.setPerson(person);

        TutorPetDomain saved = tutorPetRepository.save(tutorPet);
        return toDTO(saved);
    }

    public TutorPetDTO update(UUID id, TutorPetDTO tutorPetDTO) {
        TutorPetDomain tutorPet = getOrThrow(id);
        PersonDomain person = getPersonOrThrow(tutorPetDTO.getPerson());
        tutorPet.setPerson(person);

        TutorPetDomain updated = tutorPetRepository.save(tutorPet);
        return toDTO(updated);
    }

    public void deactivate(UUID id) {
        TutorPetDomain tutorPet = getOrThrow(id);
        personService.delete(tutorPet.getPerson().getId());
    }

    private TutorPetDomain getOrThrow(UUID id) {
        return tutorPetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "TutorPet no encontrado con id: " + id));
    }

    private PersonDomain getPersonOrThrow(UUID personId) {
        return personRepository.findById(personId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Person no encontrada con id: " + personId));
    }

    private TutorPetDTO toDTO(TutorPetDomain tutorPet) {
        TutorPetDTO dto = new TutorPetDTO();
        dto.setId(tutorPet.getId());
        dto.setPerson(tutorPet.getPerson().getId());
        return dto;
    }
}