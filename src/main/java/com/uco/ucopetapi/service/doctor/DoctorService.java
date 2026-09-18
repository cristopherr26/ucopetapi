package com.uco.ucopetapi.service.doctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import com.uco.ucopetapi.service.person.PersonService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class DoctorService {

    private final IDoctorRepository doctorRepository;
    private final PersonService personService;

    public DoctorService(IDoctorRepository doctorRepository, PersonService personService) {
        this.doctorRepository = doctorRepository;
        this.personService = personService;
    }

    // Trae todos los doctores tal cual estan en la base de datos.
    public List<DoctorDomain> findAll() {
        return doctorRepository.findAll();
    }

    // Busca un doctor por id. Si no existe, lanza BusinessException con un mensaje claro; la capa de arriba (el controller) decide que codigo HTTP
    public DoctorDomain findById(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("No se encontro un doctor con id " + id));
    }

    // Filtra por numero de licencia (busqueda parcial). Si no mandan filtro,
    // se comporta igual que findAll().
    public List<DoctorDomain> findByFilter(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.isBlank()) {
            return doctorRepository.findAll();
        }
        return doctorRepository.findByLicenseNumberContainingIgnoreCase(licenseNumber);
    }

    // Guarda un doctor nuevo. Se fuerza el id a null para que lo genere la base de datos.
    public DoctorDomain createNewDoctor(DoctorDomain doctor) {
        doctor.setId(null);
        return doctorRepository.save(doctor);
    }

    // Actualiza licencia e idPerson de un doctor existente findById ya se encarga de lanzar BusinessException si el id no existe.
    public DoctorDomain updateDoctor(UUID id, DoctorDomain doctor) {
        DoctorDomain existingDoctor = findById(id);
        existingDoctor.setLicenseNumber(doctor.getLicenseNumber());
        existingDoctor.setIdPerson(doctor.getIdPerson());
        return doctorRepository.save(existingDoctor);
    }

    // "Desactivar" un doctor no borra su fila en la tabla doctors: el estado activo/inactivo en realidad vive en Person
    public void deactivateDoctor(UUID id) {
        DoctorDomain doctor = findById(id);
        personService.delete(doctor.getIdPerson());
    }
}