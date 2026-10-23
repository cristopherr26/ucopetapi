package com.uco.ucopetapi.dto.attentionhealth;

import java.time.LocalDateTime;

/**
 * Vital-sign data belonging to an attention.
 * Null values explicitly represent measurements that were not taken.
 */
public class AttentionHealthVitalSignsDTO {

    private Double temperature;

    private Integer heartRate;

    private Integer respiratoryRate;

    private Integer systolicPressure;

    private Integer diastolicPressure;

    private Double weight;

    private Integer bodyConditionScore;

    private LocalDateTime measurementDate;

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(final Double temperature) {
        this.temperature = temperature;
    }

    public Integer getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(final Integer heartRate) {
        this.heartRate = heartRate;
    }

    public Integer getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(final Integer respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public Integer getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(final Integer systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public Integer getDiastolicPressure() {
        return diastolicPressure;
    }

    public void setDiastolicPressure(final Integer diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(final Double weight) {
        this.weight = weight;
    }

    public Integer getBodyConditionScore() {
        return bodyConditionScore;
    }

    public void setBodyConditionScore(final Integer bodyConditionScore) {
        this.bodyConditionScore = bodyConditionScore;
    }

    public LocalDateTime getMeasurementDate() {
        return measurementDate;
    }

    public void setMeasurementDate(final LocalDateTime measurementDate) {
        this.measurementDate = measurementDate;
    }
}


