package com.uco.ucopetapi.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "id_order", nullable = false, length = 20)
    private String idOrder;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "procedure_id", nullable = false)
    private UUID procedureId;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "is_authorized", nullable = false)
    private Boolean isAuthorized;

    public OrderDomain() {
    }

    @SuppressWarnings("java:S107")
    public OrderDomain(UUID id, String idOrder, UUID tutorId, UUID petId, UUID procedureId,
                       String state, LocalDateTime date, Boolean isAuthorized) {
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