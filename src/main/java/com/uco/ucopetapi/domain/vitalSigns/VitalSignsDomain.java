package com.uco.ucopetapi.domain.vitalSigns;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vital_signs")
public class VitalSignsDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "temperature")
    private double temperature;

    @Column(name = "heart_rate")
    private int heartRate;

    @Column(name = "respiratory_rate")
    private int respiratoryRate;

    @Column(name = "systolic_pressure")
    private int systolicPressure;

    @Column(name = "diastolic_pressure")
    private int diastolicPressure;

    @Column(name = "weight")
    private double weight;

    @Column(name = "body_condition_score")
    private int bodyConditionScore;

    @Column(name = "measurement_date", nullable = false)
    private LocalDateTime measurementDate;

    public VitalSignsDomain() {
    }

    public VitalSignsDomain(final UUID id, final double temperature, final int heartRate, final int respiratoryRate,
                            final int systolicPressure, final int diastolicPressure, final double weight,
                            final int bodyConditionScore, final LocalDateTime measurementDate) {
        this.id = id;
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

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(int respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public int getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(int systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public int getDiastolicPressure() {
        return diastolicPressure;
    }

    public void setDiastolicPressure(int diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public int getBodyConditionScore() {
        return bodyConditionScore;
    }

    public void setBodyConditionScore(int bodyConditionScore) {
        this.bodyConditionScore = bodyConditionScore;
    }

    public LocalDateTime getMeasurementDate() {
        return measurementDate;
    }

    public void setMeasurementDate(LocalDateTime measurementDate) {
        this.measurementDate = measurementDate;
    }
}
