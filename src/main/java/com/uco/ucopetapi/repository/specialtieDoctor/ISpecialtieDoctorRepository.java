package com.uco.ucopetapi.repository.specialtieDoctor;

import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ISpecialtieDoctorRepository extends JpaRepository<SpecialtieDoctorDomain, UUID> {
    List<SpecialtieDoctorDomain> findByDoctor_Id(UUID idDoctor);
    List<SpecialtieDoctorDomain> findByIdSpecialtie(UUID idSpecialtie);

    List<SpecialtieDoctorDomain> findByHeadquarterId(UUID headquarterId);
    List<SpecialtieDoctorDomain> findByHeadquarterIdAndDoctor_Id(UUID headquarterId, UUID idDoctor);
    List<SpecialtieDoctorDomain> findByHeadquarterIdAndIdSpecialtie(UUID headquarterId, UUID idSpecialtie);

    boolean existsByDoctor_IdAndIdSpecialtie(UUID idDoctor, UUID idSpecialtie);
}