package com.uco.ucopetapi.repository.tutorPet;

import com.uco.ucopetapi.domain.tutorPet.TutorPetDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ITutorPetRepository extends JpaRepository<TutorPetDomain, UUID> {

    List<TutorPetDomain> findByPerson_Id(UUID personId);

}