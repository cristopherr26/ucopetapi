package com.uco.ucopetapi.service.attention;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.exception.clinical.ClinicalException;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import com.uco.ucopetapi.service.person.PersonService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CurrentDoctorProvider {

    private static final String NOT_A_VETERINARIAN =
            "El usuario autenticado no está registrado como veterinario";

    private final IDoctorRepository doctorRepository;
    private final PersonService personService;

    public CurrentDoctorProvider(final IDoctorRepository doctorRepository, final PersonService personService) {
        this.doctorRepository = doctorRepository;
        this.personService = personService;
    }

    public CurrentDoctor get() {
        UUID personId = currentPersonId();
        List<DoctorDomain> doctors = doctorRepository.findByIdPerson(personId);
        if (doctors.isEmpty()) {
            throw ClinicalException.forbidden(NOT_A_VETERINARIAN);
        }

        // TODO(equipo): usar DoctorService.findByPersonId cuando exista.
        DoctorDomain doctor = doctors.getFirst();
        PersonDTO person = personService.findById(personId);
        String fullName = (person.firstName() + " " + person.lastName()).trim();
        return new CurrentDoctor(doctor.getId(), personId, fullName, doctor.getLicenseNumber());
    }

    private UUID currentPersonId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw ClinicalException.forbidden(NOT_A_VETERINARIAN);
        }
        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            throw ClinicalException.forbidden(NOT_A_VETERINARIAN);
        }
    }
}
