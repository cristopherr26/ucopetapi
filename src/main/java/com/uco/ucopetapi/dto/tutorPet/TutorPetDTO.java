package com.uco.ucopetapi.dto.tutorPet;

import java.util.UUID;

public class TutorPetDTO {

    private UUID id;
    private UUID idPerson;

    public TutorPetDTO() {
    }

    public TutorPetDTO(UUID id, UUID idPerson) {
        this.id = id;
        this.idPerson = idPerson;
    }

    public UUID getId() {

        return id;
    }

    public void setId(UUID id) {

        this.id = id;
    }

    public UUID getPerson() {

        return idPerson;
    }

    public void setPerson(UUID person) {

        this.idPerson = person;
    }
}