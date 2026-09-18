package com.uco.ucopetapi.service.specialtieDoctor;

import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.repository.specialtieDoctor.ISpecialtieDoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// Logica de negocio de la relacion Doctor-Especialidad.
@Service
public class SpecialtieDoctorService {

    private final ISpecialtieDoctorRepository specialtieDoctorRepository;

    public SpecialtieDoctorService(ISpecialtieDoctorRepository specialtieDoctorRepository) {
        this.specialtieDoctorRepository = specialtieDoctorRepository;
    }

    // Trae todas las relaciones doctor-especialidad.
    public List<SpecialtieDoctorDomain> findAll() {
        return specialtieDoctorRepository.findAll();
    }

    // Busca una relacion por su id. Igual que en DoctorService, si no existe
    // lanza BusinessException con mensaje claro.
    public SpecialtieDoctorDomain findById(UUID id) {
        return specialtieDoctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("No se encontro una relacion doctor-especialidad con id " + id));
    }

    // Filtra por doctor o por especialidad (uno u otro, no ambos a la vez).
    // Si no mandan ningun filtro, devuelve todas las relaciones.
    public List<SpecialtieDoctorDomain> findByFilter(UUID idDoctor, UUID idSpecialtie) {
        if (idDoctor != null) {
            return specialtieDoctorRepository.findByDoctor_Id(idDoctor);
        }
        if (idSpecialtie != null) {
            return specialtieDoctorRepository.findByIdSpecialtie(idSpecialtie);
        }
        return specialtieDoctorRepository.findAll();
    }

    // Guarda una relacion nueva. Se fuerza el id a null para que lo genere la
    // base de datos.
    public SpecialtieDoctorDomain createNewSpecialtieDoctor(SpecialtieDoctorDomain specialtieDoctor) {
        specialtieDoctor.setId(null);
        return specialtieDoctorRepository.save(specialtieDoctor);
    }

    // Actualiza una relacion existente (puede cambiar el doctor o la especialidad).
    public SpecialtieDoctorDomain updateSpecialtieDoctor(UUID id, SpecialtieDoctorDomain specialtieDoctor) {
        SpecialtieDoctorDomain existingSpecialtieDoctor = findById(id);
        existingSpecialtieDoctor.setDoctor(specialtieDoctor.getDoctor());
        existingSpecialtieDoctor.setIdSpecialtie(specialtieDoctor.getIdSpecialtie());
        return specialtieDoctorRepository.save(existingSpecialtieDoctor);
    }

    // Esta tabla no tiene un campo "active": la relacion doctor-especialidad
    // no tiene sentido si no esta vigente, asi que "desactivar" aqui es
    // borrar la fila de verdad.
    public void deactivateSpecialtieDoctor(UUID id) {
        SpecialtieDoctorDomain specialtieDoctor = findById(id);
        specialtieDoctorRepository.delete(specialtieDoctor);
    }
}