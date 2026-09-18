package com.uco.ucopetapi.service.specialtieDoctor;

import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.repository.specialtie.ISpecialtieRepository;
import com.uco.ucopetapi.repository.specialtieDoctor.ISpecialtieDoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SpecialtieDoctorService {

    private final ISpecialtieDoctorRepository specialtieDoctorRepository;

    // Repositorio del modulo de Especialidad, inyectado solo para validar que
    // el idSpecialtie exista antes de guardar (mismo patron que SpecialtieService).
    private final ISpecialtieRepository specialtieRepository;

    public SpecialtieDoctorService(ISpecialtieDoctorRepository specialtieDoctorRepository,
                                   ISpecialtieRepository specialtieRepository) {
        this.specialtieDoctorRepository = specialtieDoctorRepository;
        this.specialtieRepository = specialtieRepository;
    }

    public List<SpecialtieDoctorDomain> findAll() {
        return specialtieDoctorRepository.findAll();
    }

    public SpecialtieDoctorDomain findById(UUID id) {
        return specialtieDoctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("No se encontro una relacion doctor-especialidad con id " + id));
    }

    public List<SpecialtieDoctorDomain> findByFilter(UUID idDoctor, UUID idSpecialtie) {
        if (idDoctor != null) return specialtieDoctorRepository.findByDoctor_Id(idDoctor);
        if (idSpecialtie != null) return specialtieDoctorRepository.findByIdSpecialtie(idSpecialtie);
        return specialtieDoctorRepository.findAll();
    }

    public SpecialtieDoctorDomain createNewSpecialtieDoctor(SpecialtieDoctorDomain specialtieDoctor) {
        validateFields(specialtieDoctor);
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
        // Esta entidad no tiene un campo "active": "desactivar" una especialidad
        // de un doctor equivale a eliminar esa relacion.
        SpecialtieDoctorDomain specialtieDoctor = findById(id);
        specialtieDoctorRepository.delete(specialtieDoctor);
    }

    private void validateFields(SpecialtieDoctorDomain specialtieDoctor) {
        if (specialtieDoctor == null) {
            throw new BusinessException("La informacion de la relacion doctor-especialidad no puede ser nula.");
        }
        if (specialtieDoctor.getDoctor() == null || specialtieDoctor.getDoctor().getId() == null) {
            throw new BusinessException("El id del doctor es obligatorio.");
        }
        if (specialtieDoctor.getIdSpecialtie() == null) {
            throw new BusinessException("El id de la especialidad es obligatorio.");
        }
        if (!specialtieRepository.existsById(specialtieDoctor.getIdSpecialtie())) {
            throw new BusinessException("La especialidad especificada no existe.");
        }
        boolean yaExiste = specialtieDoctorRepository.existsByDoctor_IdAndIdSpecialtie(
                specialtieDoctor.getDoctor().getId(), specialtieDoctor.getIdSpecialtie());
        if (yaExiste) {
            throw new BusinessException("Este doctor ya tiene registrada esa especialidad.");
        }
    }
}