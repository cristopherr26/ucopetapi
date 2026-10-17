package com.uco.ucopetapi.service.pet;

import com.uco.ucopetapi.domain.pet.PetDomain;
import com.uco.ucopetapi.dto.pet.PetDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.repository.pet.IPetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class PetService {

    private final IPetRepository iPetRepository;
    private final PetMapper petMapper;

    private static final String TEXT_REGEX = "\\S+";
    private static final String GENDER_REGEX = "^[MF]$";
    private static final String PET_NOT_FOUND_MESSAGE =
            "No se encontró la mascota con el ID: ";

    private static final int NAME_MIN_LENGTH = 2;
    private static final int NAME_MAX_LENGTH = 100;

    private static final int BREED_MIN_LENGTH = 2;
    private static final int BREED_MAX_LENGTH = 50;

    private static final int SPECIES_MIN_LENGTH = 3;
    private static final int SPECIES_MAX_LENGTH = 50;

    private static final int PHOTO_URL_MAX_LENGTH = 500;

    private static final ZoneId ZONE_ID = ZoneId.of("America/Bogota");

    public PetService(IPetRepository iPetRepository, PetMapper petMapper) {
        this.iPetRepository = iPetRepository;
        this.petMapper = petMapper;
    }

    public List<PetDTO> getAllPets(UUID headquarterId,
                                   String name,
                                   String breed,
                                   String species,
                                   Boolean isActive) {

        List<PetDomain> domains = iPetRepository.findByFilters(
                headquarterId,
                name,
                breed,
                species,
                isActive
        );

        return petMapper.toDTOList(domains);
    }

    public PetDTO getById(UUID id) {

        PetDomain domain = iPetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        PET_NOT_FOUND_MESSAGE + id
                ));

        return petMapper.toDTO(domain);
    }

    public List<PetDTO> getPetsByTutor(UUID tutorId,
                                       UUID headquarterId) {

        List<PetDomain> domains = iPetRepository.findByTutorId(
                tutorId,
                headquarterId
        );

        return petMapper.toDTOList(domains);
    }

    @Transactional
    public PetDTO createPet(PetDTO dto) {

        validateFields(dto);

        validateUUID(dto.getTutorId().toString());

        validateUUID(dto.getHeadquarterId().toString());

        validateUUID(dto.getPolicyId().toString());

        PetDomain pet = petMapper.toDomain(dto);

        pet.setActive(true);
        pet.setCreatedDate(LocalDateTime.now(ZONE_ID));

        PetDomain savedPet = iPetRepository.save(pet);

        return petMapper.toDTO(savedPet);
    }

    @Transactional
    public PetDTO updatePet(UUID id, PetDTO dto) {

        PetDomain existingPet = iPetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        PET_NOT_FOUND_MESSAGE + id
                ));

        validateFields(dto);

        validateUUID(dto.getTutorId().toString());

        validateUUID(dto.getHeadquarterId().toString());

        validateUUID(dto.getPolicyId().toString());

        existingPet.setName(dto.getName().trim());
        existingPet.setBirthDate(dto.getBirthDate());
        existingPet.setBreed(dto.getBreed().trim());
        existingPet.setSpecies(dto.getSpecies().trim());
        existingPet.setGender(dto.getGender().trim());

        if (dto.getPhotoUrl() != null) {
            existingPet.setPhotoUrl(dto.getPhotoUrl().trim());
        } else {
            existingPet.setPhotoUrl(null);
        }

        existingPet.setTutorId(dto.getTutorId());
        existingPet.setPolicyId(dto.getPolicyId());
        existingPet.setHeadquarterId(dto.getHeadquarterId());

        existingPet.setModifiedDate(LocalDateTime.now(ZONE_ID));

        PetDomain updatedPet = iPetRepository.save(existingPet);

        return petMapper.toDTO(updatedPet);
    }

    @Transactional
    public PetDTO deactivatePet(UUID id) {

        PetDomain existingPet = iPetRepository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        PET_NOT_FOUND_MESSAGE + id
                ));

        existingPet.setActive(false);
        existingPet.setModifiedDate(LocalDateTime.now(ZONE_ID));

        PetDomain deactivatedPet = iPetRepository.save(existingPet);

        return petMapper.toDTO(deactivatedPet);
    }

    private void validateFields(PetDTO dto) {

        if (dto == null) {
            throw new BusinessException(
                    "La información de la mascota no puede ser nula."
            );
        }

        validateNameField(dto.getName());
        validateSpeciesField(dto.getSpecies());
        validateBreedField(dto.getBreed());
        validateGenderField(dto.getGender());
        validateBirthDate(dto.getBirthDate());
        validatePhotoUrl(dto.getPhotoUrl());
        validateTutorId(dto.getTutorId());
        validateHeadquarterId(dto.getHeadquarterId());
    }

    private void validateNameField(String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(
                    "El nombre de la mascota es obligatorio."
            );
        }

        if (!name.matches(TEXT_REGEX)) {
            throw new BusinessException(
                    "El nombre de la mascota no puede contener espacios."
            );
        }

        validateName(name);
    }

    private void validateSpeciesField(String species) {

        if (species == null || species.trim().isEmpty()) {
            throw new BusinessException(
                    "La especie es obligatoria."
            );
        }

        if (!species.matches(TEXT_REGEX)) {
            throw new BusinessException(
                    "La especie no puede contener espacios."
            );
        }

        validateSpecies(species);
    }

    private void validateBreedField(String breed) {

        if (breed == null || breed.trim().isEmpty()) {
            throw new BusinessException(
                    "La raza es obligatoria."
            );
        }

        if (!breed.matches(TEXT_REGEX)) {
            throw new BusinessException(
                    "La raza no puede contener espacios."
            );
        }

        validateBreed(breed);
    }

    private void validateGenderField(String gender) {

        if (gender == null || gender.trim().isEmpty()) {
            throw new BusinessException(
                    "El género es obligatorio."
            );
        }

        if (!gender.trim().matches(GENDER_REGEX)) {
            throw new BusinessException(
                    "El género debe ser M o F."
            );
        }
    }

    private void validateBirthDate(LocalDate birthDate) {

        if (birthDate != null
                && birthDate.isAfter(LocalDate.now(ZONE_ID))) {

            throw new BusinessException(
                    "La fecha de nacimiento no puede ser futura."
            );
        }
    }

    private void validatePhotoUrl(String photoUrl) {

        if (photoUrl != null
                && photoUrl.trim().length() > PHOTO_URL_MAX_LENGTH) {

            throw new BusinessException(
                    "La URL de la foto no puede superar los "
                            + PHOTO_URL_MAX_LENGTH + " caracteres."
            );
        }
    }

    private void validateTutorId(UUID tutorId) {

        if (tutorId == null) {
            throw new BusinessException(
                    "El ID del tutor es obligatorio."
            );
        }
    }

    private void validateHeadquarterId(UUID headquarterId) {

        if (headquarterId == null) {
            throw new BusinessException(
                    "El ID de la sede es obligatorio."
            );
        }
    }

    private void validateName(String name) {

        String nameTrimmed = name.trim();

        if (nameTrimmed.length() < NAME_MIN_LENGTH
                || nameTrimmed.length() > NAME_MAX_LENGTH) {

            throw new BusinessException(
                    "El nombre debe tener una longitud de entre "
                            + NAME_MIN_LENGTH + " y "
                            + NAME_MAX_LENGTH + " caracteres."
            );
        }
    }

    private void validateBreed(String breed) {

        String breedTrimmed = breed.trim();

        if (breedTrimmed.length() < BREED_MIN_LENGTH
                || breedTrimmed.length() > BREED_MAX_LENGTH) {

            throw new BusinessException(
                    "La raza debe tener una longitud de entre "
                            + BREED_MIN_LENGTH + " y "
                            + BREED_MAX_LENGTH + " caracteres."
            );
        }
    }

    private void validateSpecies(String species) {

        String speciesTrimmed = species.trim();

        if (speciesTrimmed.length() < SPECIES_MIN_LENGTH
                || speciesTrimmed.length() > SPECIES_MAX_LENGTH) {

            throw new BusinessException(
                    "La especie debe tener una longitud de entre "
                            + SPECIES_MIN_LENGTH + " y "
                            + SPECIES_MAX_LENGTH + " caracteres."
            );
        }
    }

    public UUID validateUUID(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            throw new BusinessException("El ID proporcionado no puede ser nulo o estar vacío.");
        }

        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException _) {
            throw new BusinessException("El tipo de dato proporcionado no es un UUID válido: " + uuid);
        }
    }
}