package com.uco.ucopetapi.domain.vitalSigns;

import com.uco.ucopetapi.domain.petCare.PetCareDomain;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vital_signs")
public class VitalSignsDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_care_id", nullable = false)
    private PetCareDomain petCare;

    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "systolic_pressure")
    private Integer systolicPressure;

    @Column(name = "diastolic_pressure")
    private Integer diastolicPressure;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "body_condition_score")
    private Integer bodyConditionScore;

    @Column(name = "measurement_date", nullable = false)
    private LocalDateTime measurementDate;

    public VitalSignsDomain() {
    }

    public VitalSignsDomain(final UUID id, final PetCareDomain petCare, final Double temperature,
                            final Integer heartRate, final Integer respiratoryRate,
                            final Integer systolicPressure, final Integer diastolicPressure,
                            final Double weight, final Integer bodyConditionScore,
                            final LocalDateTime measurementDate) {
        this.id = id;
        this.petCare = petCare;
        this.temperature = temperature;
        this.heartRate = heartRate;
        this.respiratoryRate = respiratoryRate;
        this.systolicPressure = systolicPressure;
        this.diastolicPressure = diastolicPressure;
        this.weight = weight;
        this.bodyConditionScore = bodyConditionScore;
        this.measurementDate = measurementDate;
    }

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (measurementDate == null) {
            measurementDate = LocalDateTime.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PetCareDomain getPetCare() {
        return petCare;
    }

    public void setPetCare(PetCareDomain petCare) {
        this.petCare = petCare;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Integer getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(Integer systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public Integer getDiastolicPressure() {
        return diastolicPressure;
    }

    public void setDiastolicPressure(Integer diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Integer getBodyConditionScore() {
        return bodyConditionScore;
    }

    public void setBodyConditionScore(Integer bodyConditionScore) {
        this.bodyConditionScore = bodyConditionScore;
    }

    public LocalDateTime getMeasurementDate() {
        return measurementDate;
    }

    public void setMeasurementDate(LocalDateTime measurementDate) {
        this.measurementDate = measurementDate;
    }
}
