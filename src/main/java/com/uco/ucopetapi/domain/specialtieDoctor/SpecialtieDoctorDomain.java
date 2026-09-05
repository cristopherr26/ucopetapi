package com.uco.ucopetapi.domain.specialtieDoctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "specialtie_doctors")
public class SpecialtieDoctorDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doctor", nullable = false)
    private DoctorDomain doctor;

    @Column(name = "id_specialtie", nullable = false)
    private UUID idSpecialtie;


    public SpecialtieDoctorDomain() {
    }

    public SpecialtieDoctorDomain(UUID id, DoctorDomain doctor, UUID idSpecialtie) {
        this.id = id;
        this.doctor = doctor;
        this.idSpecialtie = idSpecialtie;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public DoctorDomain getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorDomain doctor) {
        this.doctor = doctor;
    }

    public UUID getIdSpecialtie() {
        return idSpecialtie;
    }

    public void setIdSpecialtie(UUID idSpecialtie) {
        this.idSpecialtie = idSpecialtie;
    }
}