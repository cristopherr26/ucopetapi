package com.uco.ucopetapi.service.doctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.repository.doctor.IDoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class DoctorService {

    private final IDoctorRepository doctorRepository;

    public DoctorService(IDoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<DoctorDomain> findAll() {
        return doctorRepository.findAll();
    }

    public DoctorDomain findById(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontro un doctor con id " + id));
    }

    public List<DoctorDomain> findByFilter(String licenseNumber) {
        if (licenseNumber == null || licenseNumber.isBlank()) {
            return doctorRepository.findAll();
        }
        return doctorRepository.findByLicenseNumberContainingIgnoreCase(licenseNumber);
    }

    public DoctorDomain createNewDoctor(DoctorDomain doctor) {
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
        //llamar a person service cuando la cree pablo
        findById(id);
    }
}
