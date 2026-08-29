package com.uco.ucopetapi.dto.pets;

import java.time.LocalDate;
import java.util.UUID;

public class PetDTO {
    private UUID id;
    private String name;
    private LocalDate birthDate;
    private String breed;
    private String species;
    private String gender;
    private String photoUrl;
    private UUID tutorId;
    private UUID policyId;
    private boolean isActive;

    public PetDTO() {
    }

    public PetDTO(final UUID id, final String name, final LocalDate birthDate, final String breed,
                  final String species, final String gender, final String photoUrl,
                  final UUID tutorId, final UUID policyId, final boolean isActive) {
        setId(id);
        setName(name);
        setBirthDate(birthDate);
        setBreed(breed);
        setSpecies(species);
        setGender(gender);
        setPhotoUrl(photoUrl);
        setTutorId(tutorId);
        setPolicyId(policyId);
        setActive(isActive);
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(final LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(final String breed) {
        this.breed = breed;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(final String species) {
        this.species = species;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(final String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public UUID getTutorId() {
        return tutorId;
    }

    public void setTutorId(final UUID tutorId) {
        this.tutorId = tutorId;
    }

    public UUID getPolicyId() {
        return policyId;
    }

    public void setPolicyId(final UUID policyId) {
        this.policyId = policyId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(final boolean active) {
        isActive = active;
    }
}