package com.uco.ucopetapi.domain.order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "id_order", nullable = false, unique = true, insertable = false, updatable = false,
            columnDefinition = "BIGSERIAL")
    @Generated(event = EventType.INSERT)
    private Long idOrder;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "procedure_id", nullable = false)
    private UUID procedureId;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private OrderState state;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "is_authorized", nullable = false)
    private Boolean isAuthorized;

    public OrderDomain() {
    }

    @SuppressWarnings("java:S107")
    public OrderDomain(UUID id, Long idOrder, UUID tutorId, UUID petId, UUID procedureId,
                       OrderState state, LocalDateTime date, Boolean isAuthorized) {
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

    public Long getIdOrder() { return idOrder; }
    public void setIdOrder(Long idOrder) { this.idOrder = idOrder; }

    public UUID getTutorId() { return tutorId; }
    public void setTutorId(UUID tutorId) { this.tutorId = tutorId; }

    public UUID getPetId() { return petId; }
    public void setPetId(UUID petId) { this.petId = petId; }

    public UUID getProcedureId() { return procedureId; }
    public void setProcedureId(UUID procedureId) { this.procedureId = procedureId; }

    public OrderState getState() { return state; }
    public void setState(OrderState state) { this.state = state; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Boolean getIsAuthorized() { return isAuthorized; }
    public void setIsAuthorized(Boolean isAuthorized) { this.isAuthorized = isAuthorized; }
}