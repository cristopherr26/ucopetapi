package com.uco.ucopetapi.dto.vitalSigns;

import java.time.LocalDateTime;
import java.util.UUID;

public class VitalSignsDTO {
    private UUID id;
    private Double temperature;
    private Integer heartRate;
    private Integer respiratoryRate;
    private Integer systolicPressure;
    private Integer diastolicPressure;
    private Double weight;
    private Integer bodyConditionScore;
    private LocalDateTime measurementDate;

    public VitalSignsDTO() {
    }

    public VitalSignsDTO(final UUID id, final Double temperature, final Integer heartRate,
                         final Integer respiratoryRate, final Integer systolicPressure,
                         final Integer diastolicPressure, final Double weight,
                         final Integer bodyConditionScore, final LocalDateTime measurementDate) {
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
