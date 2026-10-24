package com.uco.ucopetapi.domain.attentionhealth;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Persistence model for the new veterinary attention functionality.
 * External modules are referenced only by their identifiers.
 */
@Entity
@Table(name = "attention_health")
public class AttentionHealthDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attentionId;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    @Column(name = "doctor_id", nullable = false)
    private UUID doctorId;

    @Column(name = "appointment_id", unique = true)
    private UUID appointmentId;

    @Column(name = "headquarter_id", nullable = false)
    private UUID headquarterId;

    @Column(name = "attention_date", nullable = false)
    private LocalDateTime attentionDate;

    @Column(name = "attention_type", nullable = false, length = 80)
    private String attentionType;

    @Column(length = 500)
    private String reason;

    @Column(name = "clinical_assessment", length = 2000)
    private String clinicalAssessment;

    @Column(length = 1000)
    private String observations;

    @Column(length = 1000)
    private String diagnosis;

    @Column(nullable = false, length = 30)
    private String status;

    @Column
    private Double temperature;

    @Column(name = "heart_rate")
    private Integer heartRate;

    @Column(name = "respiratory_rate")
    private Integer respiratoryRate;

    @Column(name = "systolic_pressure")
    private Integer systolicPressure;

    @Column(name = "diastolic_pressure")
    private Integer diastolicPressure;

    @Column
    private Double weight;

    @Column(name = "body_condition_score")
    private Integer bodyConditionScore;

    @Column(name = "measurement_date")
    private LocalDateTime measurementDate;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "attention_health_products", joinColumns = @JoinColumn(name = "attention_id"))
    private List<AttentionHealthProductDomain> products = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "attention_health_procedures", joinColumns = @JoinColumn(name = "attention_id"))
    @Column(name = "procedure_id", nullable = false)
    private List<UUID> procedureIds = new ArrayList<>();

    public AttentionHealthDomain() {
        // Required by JPA.
    }

    public Long getAttentionId() {
        return attentionId;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(final UUID petId) {
        this.petId = petId;
    }

    public UUID getTutorId() {
        return tutorId;
    }

    public void setTutorId(final UUID tutorId) {
        this.tutorId = tutorId;
    }

    public UUID getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(final UUID doctorId) {
        this.doctorId = doctorId;
    }

    public UUID getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(final UUID appointmentId) {
        this.appointmentId = appointmentId;
    }

    public UUID getHeadquarterId() {
        return headquarterId;
    }

    public void setHeadquarterId(final UUID headquarterId) {
        this.headquarterId = headquarterId;
    }

    public LocalDateTime getAttentionDate() {
        return attentionDate;
    }

    public void setAttentionDate(final LocalDateTime attentionDate) {
        this.attentionDate = attentionDate;
    }

    public String getAttentionType() {
        return attentionType;
    }

    public void setAttentionType(final String attentionType) {
        this.attentionType = attentionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public String getClinicalAssessment() {
        return clinicalAssessment;
    }

    public void setClinicalAssessment(final String clinicalAssessment) {
        this.clinicalAssessment = clinicalAssessment;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(final String observations) {
        this.observations = observations;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(final String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

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

    public List<AttentionHealthProductDomain> getProducts() {
        return new ArrayList<>(products);
    }

    public void setProducts(final List<AttentionHealthProductDomain> products) {
        this.products = products == null ? new ArrayList<>() : new ArrayList<>(products);
    }

    public List<UUID> getProcedureIds() {
        return new ArrayList<>(procedureIds);
    }

    public void setProcedureIds(final List<UUID> procedureIds) {
        this.procedureIds = procedureIds == null ? new ArrayList<>() : new ArrayList<>(procedureIds);
    }
}


