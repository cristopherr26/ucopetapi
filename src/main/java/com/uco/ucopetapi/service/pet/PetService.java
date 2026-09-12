package com.uco.ucopetapi.service.pet;

import com.uco.ucopetapi.domain.pet.PetDomain;
import com.uco.ucopetapi.dto.pets.PetDTO;
import com.uco.ucopetapi.repository.pet.IPetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PetService {

    @Autowired
    private IPetRepository iPetRepository;

    public List<PetDTO> getAllPets(UUID headquarterId, String name, String breed, String species, Boolean isActive) {
        return iPetRepository.findByFilters(headquarterId, name, breed, species, isActive)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PetDTO getById(UUID id) {
        PetDomain pet = iPetRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Mascota no encontrada: " + id));
        return toDTO(pet);
    }

    public PetDTO createPet(PetDTO petDTO) {

        validationData(petDTO);

        PetDomain pet = new PetDomain();
        pet.setName(petDTO.getName());
        pet.setBirthDate(petDTO.getBirthDate());
        pet.setBreed(petDTO.getBreed());
        pet.setSpecies(petDTO.getSpecies());
        pet.setGender(petDTO.getGender());
        pet.setPhotoUrl(petDTO.getPhotoUrl());
        pet.setTutorId(petDTO.getTutorId());
        pet.setPolicyId(petDTO.getPolicyId());
        pet.setHeadquarterId(petDTO.getHeadquarterId());
        pet.setActive(true);
        pet.setCreatedDate(LocalDateTime.now());

        PetDomain savedPet = iPetRepository.save(pet);
        return toDTO(savedPet);
    }

    public PetDTO updatePet(UUID petId, PetDTO petDTO) {

        validationData(petDTO);

        PetDomain existingPet = iPetRepository.findById(petId)
                .orElseThrow(() -> new NoSuchElementException("Mascota no encontrada: " + petId));

        existingPet.setName(isEmpty(petDTO.getName()) ? existingPet.getName() : petDTO.getName());
        existingPet.setBreed(isEmpty(petDTO.getBreed()) ? existingPet.getBreed() : petDTO.getBreed());
        existingPet.setSpecies(isEmpty(petDTO.getSpecies()) ? existingPet.getSpecies() : petDTO.getSpecies());
        existingPet.setGender(isEmpty(petDTO.getGender()) ? existingPet.getGender() : petDTO.getGender());
        existingPet.setPhotoUrl(isEmpty(petDTO.getPhotoUrl()) ? existingPet.getPhotoUrl() : petDTO.getPhotoUrl());
        existingPet.setBirthDate(
                Objects.isNull(petDTO.getBirthDate()) ? existingPet.getBirthDate() : petDTO.getBirthDate());
        existingPet.setTutorId(
                Objects.isNull(petDTO.getTutorId()) ? existingPet.getTutorId() : petDTO.getTutorId());
        existingPet.setPolicyId(
                Objects.isNull(petDTO.getPolicyId()) ? existingPet.getPolicyId() : petDTO.getPolicyId());
        existingPet.setHeadquarterId(
                Objects.isNull(petDTO.getHeadquarterId()) ? existingPet.getHeadquarterId() : petDTO.getHeadquarterId());
        existingPet.setCreatedDate(existingPet.getCreatedDate());
        existingPet.setModifiedDate(LocalDateTime.now());

        PetDomain updatedPet = iPetRepository.save(existingPet);
        return toDTO(updatedPet);
    }

    public List<PetDTO> getPetsByTutor(UUID tutorId, UUID headquarterId) {
        return iPetRepository.findByTutorId(tutorId, headquarterId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public PetDTO deactivatePet(UUID petId) {

        PetDomain existingPet = iPetRepository.findById(petId)
                .orElseThrow(() -> new NoSuchElementException("Mascota no encontrada: " + petId));

        existingPet.setActive(false);
        existingPet.setModifiedDate(LocalDateTime.now());

        PetDomain deactivatedPet = iPetRepository.save(existingPet);
        return toDTO(deactivatedPet);
    }

    private void validationData(PetDTO petDTO) {

        if (Objects.isNull(petDTO.getName()) || petDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la mascota es obligatorio");
        }
        if (Objects.isNull(petDTO.getSpecies()) || petDTO.getSpecies().isEmpty()) {
            throw new IllegalArgumentException("La especie es obligatoria");
        }
        if (Objects.isNull(petDTO.getTutorId())) {
            throw new IllegalArgumentException("La mascota debe estar asociada a un tutor");
        }
        if (Objects.isNull(petDTO.getHeadquarterId())) {
            throw new IllegalArgumentException("La mascota debe estar asociada a una sede");
        }
    }

    private boolean isEmpty(String value) {
        return Objects.isNull(value) || value.isEmpty();
    }

    private PetDTO toDTO(PetDomain pet) {
        return new PetDTO(
                pet.getId(),
                pet.getName(),
                pet.getBirthDate(),
                pet.getBreed(),
                pet.getSpecies(),
                pet.getGender(),
                pet.getPhotoUrl(),
                pet.getTutorId(),
                pet.getPolicyId(),
                pet.getHeadquarterId(),
                pet.isActive()
        );
    }
}