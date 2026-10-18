package com.uco.ucopetapi.dto.attentionhealth;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Initial transport contract for creating and updating an attention.
 * Related entities are represented by identifiers and are not duplicated.
 */
public class AttentionHealthDTO {

    private Long attentionId;
    private UUID petId;
    private UUID tutorId;
    private UUID doctorId;
    private UUID appointmentId;
    private UUID headquarterId;

    private LocalDateTime attentionDate;

    private String attentionType;

    private String reason;

    private String clinicalAssessment;

    private String observations;

    private String diagnosis;

    private String status;

    private AttentionHealthVitalSignsDTO vitalSigns;

    private List<AttentionHealthProductDTO> products = new ArrayList<>();
    private List<UUID> procedureIds = new ArrayList<>();

    public AttentionHealthDTO() {
        // Required by Jackson and Spring for request-body deserialization.
    }

    public Long getAttentionId() {
        return attentionId;
    }

    public void setAttentionId(final Long attentionId) {
        this.attentionId = attentionId;
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

    public AttentionHealthVitalSignsDTO getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(final AttentionHealthVitalSignsDTO vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public List<AttentionHealthProductDTO> getProducts() {
        return products;
    }

    public void setProducts(final List<AttentionHealthProductDTO> products) {
        this.products = products == null ? new ArrayList<>() : products;
    }

    public List<UUID> getProcedureIds() {
        return procedureIds;
    }

    public void setProcedureIds(final List<UUID> procedureIds) {
        this.procedureIds = procedureIds == null ? new ArrayList<>() : procedureIds;
    }
}


