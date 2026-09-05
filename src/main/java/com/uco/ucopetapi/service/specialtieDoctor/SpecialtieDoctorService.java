package com.uco.ucopetapi.service.specialtieDoctor;

import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.repository.specialtieDoctor.ISpecialtieDoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SpecialtieDoctorService {

    private final ISpecialtieDoctorRepository specialtieDoctorRepository;

    public SpecialtieDoctorService(ISpecialtieDoctorRepository specialtieDoctorRepository) {
        this.specialtieDoctorRepository = specialtieDoctorRepository;
    }

    public List<SpecialtieDoctorDomain> findAll() {
        return specialtieDoctorRepository.findAll();
    }

    public SpecialtieDoctorDomain findById(UUID id) {
        return specialtieDoctorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No se encontro una relacion doctor-especialidad con id " + id));
    }

    public List<SpecialtieDoctorDomain> findByFilter(UUID idDoctor, UUID idSpecialtie) {
        if (idDoctor != null) {
            return specialtieDoctorRepository.findByDoctor_Id(idDoctor);
        }
        if (idSpecialtie != null) {
            return specialtieDoctorRepository.findByIdSpecialtie(idSpecialtie);
        }
        return specialtieDoctorRepository.findAll();
    }

    public SpecialtieDoctorDomain createNewSpecialtieDoctor(SpecialtieDoctorDomain specialtieDoctor) {
        specialtieDoctor.setId(null);
        return specialtieDoctorRepository.save(specialtieDoctor);
    }

    public SpecialtieDoctorDomain updateSpecialtieDoctor(UUID id, SpecialtieDoctorDomain specialtieDoctor) {
        SpecialtieDoctorDomain existingSpecialtieDoctor = findById(id);
        existingSpecialtieDoctor.setDoctor(specialtieDoctor.getDoctor());
        existingSpecialtieDoctor.setIdSpecialtie(specialtieDoctor.getIdSpecialtie());
        return specialtieDoctorRepository.save(existingSpecialtieDoctor);
    }

    public void deactivateSpecialtieDoctor(UUID id) {
        SpecialtieDoctorDomain specialtieDoctor = findById(id);
        specialtieDoctorRepository.delete(specialtieDoctor);
    }
}