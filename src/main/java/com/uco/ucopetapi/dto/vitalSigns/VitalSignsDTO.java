package com.uco.ucopetapi.dto.vitalSigns;

import java.time.LocalDateTime;

public class VitalSignsDTO {
    private final double temperature;
    private final int heartRate;
    private final int respiratoryRate;
    private final int systolicPressure;
    private final int diastolicPressure;
    private final double weight;
    private final int bodyConditionScore;
    private final LocalDateTime measurementDate;

    public VitalSignsDTO(double temperature, int heartRate, int respiratoryRate, int systolicPressure,
                         int diastolicPressure, double weight, int bodyConditionScore, LocalDateTime measurementDate){
        this.temperature = temperature;
        this.heartRate = heartRate;
        this.respiratoryRate = respiratoryRate;
        this.systolicPressure = systolicPressure;
        this.diastolicPressure = diastolicPressure;
        this.weight = weight;
        this.bodyConditionScore = bodyConditionScore;
        this.measurementDate = measurementDate;
    }

    public double getTemperature() {
        return temperature;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public int getRespiratoryRate() {
        return respiratoryRate;
    }

    public int getSystolicPressure() {
        return systolicPressure;
    }

    public int getDiastolicPressure() {
        return diastolicPressure;
    }

    public double getWeight() {
        return weight;
    }

    public int getBodyConditionScore() {
        return bodyConditionScore;
    }

    public LocalDateTime getMeasurementDate() {
        return measurementDate;
    }
}
