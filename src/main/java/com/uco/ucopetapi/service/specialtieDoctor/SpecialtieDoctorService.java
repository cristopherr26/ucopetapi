package com.uco.ucopetapi.service.specialtieDoctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.messaging.specialtieDoctor.MessageSenderBrokerSpecialtieDoctorCreate;
import com.uco.ucopetapi.messaging.specialtieDoctor.MessageSenderBrokerSpecialtieDoctorDeactivate;
import com.uco.ucopetapi.messaging.specialtieDoctor.MessageSenderBrokerSpecialtieDoctorUpdate;
import com.uco.ucopetapi.repository.specialtie.ISpecialtieRepository;
import com.uco.ucopetapi.repository.specialtieDoctor.ISpecialtieDoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SpecialtieDoctorService {

    private final ISpecialtieDoctorRepository specialtieDoctorRepository;
    private final ISpecialtieRepository specialtieRepository;
    private final MessageSenderBrokerSpecialtieDoctorCreate specialtieDoctorCreateSenderBroker;
    private final MessageSenderBrokerSpecialtieDoctorUpdate specialtieDoctorUpdateSenderBroker;
    private final MessageSenderBrokerSpecialtieDoctorDeactivate specialtieDoctorDeactivateSenderBroker;

    public SpecialtieDoctorService(ISpecialtieDoctorRepository specialtieDoctorRepository,
                                   ISpecialtieRepository specialtieRepository,
                                   MessageSenderBrokerSpecialtieDoctorCreate specialtieDoctorCreateSenderBroker,
                                   MessageSenderBrokerSpecialtieDoctorUpdate specialtieDoctorUpdateSenderBroker,
                                   MessageSenderBrokerSpecialtieDoctorDeactivate specialtieDoctorDeactivateSenderBroker) {
        this.specialtieDoctorRepository = specialtieDoctorRepository;
        this.specialtieRepository = specialtieRepository;
        this.specialtieDoctorCreateSenderBroker = specialtieDoctorCreateSenderBroker;
        this.specialtieDoctorUpdateSenderBroker = specialtieDoctorUpdateSenderBroker;
        this.specialtieDoctorDeactivateSenderBroker = specialtieDoctorDeactivateSenderBroker;
    }

    public List<SpecialtieDoctorDomain> findAll(UUID headquarterId) {
        validateHeadquarterId(headquarterId);
        return specialtieDoctorRepository.findByHeadquarterId(headquarterId);
    }

    public SpecialtieDoctorDomain findById(UUID id) {
        return specialtieDoctorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("No se encontro una relacion doctor-especialidad con id " + id));
    }

    public List<SpecialtieDoctorDomain> findByFilter(UUID headquarterId, UUID idDoctor, UUID idSpecialtie) {
        validateHeadquarterId(headquarterId);
        if (idDoctor != null) {
            return specialtieDoctorRepository.findByHeadquarterIdAndDoctor_Id(headquarterId, idDoctor);
        }
        if (idSpecialtie != null) {
            return specialtieDoctorRepository.findByHeadquarterIdAndIdSpecialtie(headquarterId, idSpecialtie);
        }
        return specialtieDoctorRepository.findByHeadquarterId(headquarterId);
    }

    public SpecialtieDoctorDomain createNewSpecialtieDoctor(SpecialtieDoctorDomain specialtieDoctor) {
        validateFieldsForCreate(specialtieDoctor);
        specialtieDoctor.setId(null);

        String idMessage = UUID.randomUUID().toString();
        specialtieDoctorCreateSenderBroker.execute(specialtieDoctor, idMessage);

        return specialtieDoctor;
    }

    public SpecialtieDoctorDomain updateSpecialtieDoctor(UUID id, SpecialtieDoctorDomain specialtieDoctor) {
        SpecialtieDoctorDomain existingSpecialtieDoctor = findById(id);

        validateFieldsForUpdate(existingSpecialtieDoctor, specialtieDoctor);

        existingSpecialtieDoctor.setDoctor(specialtieDoctor.getDoctor());
        existingSpecialtieDoctor.setIdSpecialtie(specialtieDoctor.getIdSpecialtie());
        existingSpecialtieDoctor.setHeadquarterId(specialtieDoctor.getHeadquarterId());

        String idMessage = UUID.randomUUID().toString();
        specialtieDoctorUpdateSenderBroker.execute(existingSpecialtieDoctor, idMessage);

        return existingSpecialtieDoctor;
    }

    public void deactivateSpecialtieDoctor(UUID id) {

        SpecialtieDoctorDomain specialtieDoctor = findById(id);


        DoctorDomain plainDoctor = new DoctorDomain();
        plainDoctor.setId(specialtieDoctor.getDoctor().getId());
        specialtieDoctor.setDoctor(plainDoctor);

        String idMessage = UUID.randomUUID().toString();
        specialtieDoctorDeactivateSenderBroker.execute(specialtieDoctor, idMessage);
    }

    private void validateFieldsForCreate(SpecialtieDoctorDomain specialtieDoctor) {
        validateCommonFields(specialtieDoctor);

        boolean yaExiste = specialtieDoctorRepository.existsByDoctor_IdAndIdSpecialtie(
                specialtieDoctor.getDoctor().getId(), specialtieDoctor.getIdSpecialtie());
        if (yaExiste) {
            throw new BusinessException("Este doctor ya tiene registrada esa especialidad.");
        }
    }

    private void validateFieldsForUpdate(SpecialtieDoctorDomain existingSpecialtieDoctor, SpecialtieDoctorDomain specialtieDoctor) {
        validateCommonFields(specialtieDoctor);

        UUID newDoctorId = specialtieDoctor.getDoctor().getId();
        UUID newSpecialtieId = specialtieDoctor.getIdSpecialtie();

        boolean doctorChanged = !newDoctorId.equals(existingSpecialtieDoctor.getDoctor().getId());
        boolean specialtieChanged = !newSpecialtieId.equals(existingSpecialtieDoctor.getIdSpecialtie());

        if ((doctorChanged || specialtieChanged)
                && specialtieDoctorRepository.existsByDoctor_IdAndIdSpecialtie(newDoctorId, newSpecialtieId)) {
            throw new BusinessException("Este doctor ya tiene registrada esa especialidad.");
        }
    }

    private void validateCommonFields(SpecialtieDoctorDomain specialtieDoctor) {
        if (specialtieDoctor == null) {
            throw new BusinessException("La informacion de la relacion doctor-especialidad no puede ser nula.");
        }
        if (specialtieDoctor.getDoctor() == null || specialtieDoctor.getDoctor().getId() == null) {
            throw new BusinessException("El id del doctor es obligatorio.");
        }
        if (specialtieDoctor.getIdSpecialtie() == null) {
            throw new BusinessException("El id de la especialidad es obligatorio.");
        }

        validateUUID(specialtieDoctor.getDoctor().getId().toString());
        validateUUID(specialtieDoctor.getIdSpecialtie().toString());
        validateHeadquarterId(specialtieDoctor.getHeadquarterId());

        if (!specialtieDoctor.getHeadquarterId().equals(specialtieDoctor.getDoctor().getHeadquarterId())) {
            throw new BusinessException("El doctor no pertenece a la sede indicada.");
        }

        if (!specialtieRepository.existsById(specialtieDoctor.getIdSpecialtie())) {
            throw new BusinessException("La especialidad especificada no existe.");
        }
    }

    private void validateHeadquarterId(UUID headquarterId) {
        if (headquarterId == null) {
            throw new BusinessException("El ID de la sede es obligatorio.");
        }
        validateUUID(headquarterId.toString());
    }

    public UUID validateUUID(String uuid) {
        if (uuid == null || uuid.trim().isEmpty()) {
            throw new BusinessException("El ID proporcionado no puede ser nulo o estar vacio.");
        }

        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException _) {
            throw new BusinessException("El tipo de dato proporcionado no es un UUID valido: " + uuid);
        }
    }
}