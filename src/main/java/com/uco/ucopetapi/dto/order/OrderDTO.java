package com.uco.ucopetapi.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderDTO {

    private UUID id;

    private String idOrder;

    private UUID tutorId;

    private UUID petId;

    private UUID procedureId;

    private String state;

    private LocalDateTime date;

    private Boolean isAuthorized;

    public OrderDTO() {
    }

    @SuppressWarnings("java:S107")
    public OrderDTO(UUID id, String idOrder, UUID tutorId, UUID petId, UUID procedureId, String state,
                    LocalDateTime date, Boolean isAuthorized) {
        this.id = id;
        this.idOrder = idOrder;
        this.tutorId = tutorId;
        this.petId = petId;
        this.procedureId = procedureId;
        this.state = state;
        this.date = date;
        this.isAuthorized = isAuthorized;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getIdOrder() { return idOrder; }
    public void setIdOrder(String idOrder) { this.idOrder = idOrder; }

    public UUID getTutorId() { return tutorId; }
    public void setTutorId(UUID tutorId) { this.tutorId = tutorId; }

    public UUID getPetId() { return petId; }
    public void setPetId(UUID petId) { this.petId = petId; }

    public UUID getProcedureId() { return procedureId; }
    public void setProcedureId(UUID procedureId) { this.procedureId = procedureId; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Boolean getAuthorized() { return isAuthorized; }
    public void setAuthorized(Boolean authorized) { isAuthorized = authorized; }
}