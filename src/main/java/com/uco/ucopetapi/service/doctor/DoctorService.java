package com.uco.ucopetapi.service.doctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.dto.person.PersonDTO;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import com.uco.ucopetapi.service.person.PersonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DoctorService {

    private static final String LICENSE_NUMBER_REGEX = "^[A-Za-z0-9-]+$";

    private final IDoctorRepository doctorRepository;
    private final PersonService personService;

    public DoctorService(IDoctorRepository doctorRepository, PersonService personService) {
        this.doctorRepository = doctorRepository;
        this.personService = personService;
    }

    public List<DoctorDomain> findAll() {
        return doctorRepository.findAll();
    }

    public DoctorDomain findById(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("No se encontro un doctor con id " + id));
    }

    public List<DoctorDomain> findByFilter(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.isBlank()) {
            return doctorRepository.findAll();
        }
        return doctorRepository.findByLicenseNumberContainingIgnoreCase(licenseNumber);
    }

    public DoctorDomain createNewDoctor(DoctorDomain doctor) {
        validateFields(doctor);
        doctor.setId(null);
        return doctorRepository.save(doctor);
    }

    public DoctorDomain updateDoctor(UUID id, DoctorDomain doctor) {
        DoctorDomain existingDoctor = findById(id);
        existingDoctor.setLicenseNumber(doctor.getLicenseNumber());
        existingDoctor.setIdPerson(doctor.getIdPerson());
        return doctorRepository.save(existingDoctor);
    }

    public void deactivateDoctor(UUID id) {
        // DoctorDomain no tiene su propio campo "active": ese estado vive en Person.
        DoctorDomain doctor = findById(id);
        personService.delete(doctor.getIdPerson());
    }

    public void activateDoctor(UUID id) {
        // Mismo enfoque que deactivateDoctor: no toco PersonService, solo uso
        // su API publica (findById/update) para reactivar la Person asociada.
        DoctorDomain doctor = findById(id);
        PersonDTO current = personService.findById(doctor.getIdPerson());

        PersonDTO reactivated = new PersonDTO(
                current.id(), current.documentType(), current.documentNumber(),
                current.firstName(), current.lastName(), current.email(),
                current.address(), current.phone(), current.admin(),
                true, current.deactivatedAt()
        );

        personService.update(doctor.getIdPerson(), reactivated);
    }

    private void validateFields(DoctorDomain doctor) {
        if (doctor == null) {
            throw new BusinessException("La informacion del doctor no puede ser nula.");
        }
        if (doctor.getIdPerson() == null) {
            throw new BusinessException("El id de la persona es obligatorio.");
        }
        String licenseNumber = doctor.getLicenseNumber();
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            throw new BusinessException("El numero de licencia es obligatorio.");
        }
        String licenseTrimmed = licenseNumber.trim();
        if (licenseTrimmed.length() < 4 || licenseTrimmed.length() > 30) {
            throw new BusinessException("El numero de licencia debe tener una longitud de entre 4 y 30 caracteres.");
        }
        if (!licenseTrimmed.matches(LICENSE_NUMBER_REGEX)) {
            throw new BusinessException("El numero de licencia solo puede contener letras, numeros y guiones.");
        }
        if (doctorRepository.existsByIdPerson(doctor.getIdPerson())) {
            throw new BusinessException("Esta persona ya esta registrada como doctor.");
        }
        if (doctorRepository.existsByLicenseNumber(licenseTrimmed)) {
            throw new BusinessException("Ya existe un doctor registrado con ese numero de licencia.");
        }
    }
}