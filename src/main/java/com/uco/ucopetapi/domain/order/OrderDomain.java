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

    @Column(name = "tutor", nullable = false, length = 100)
    private String tutor;

    @Column(name = "pet", nullable = false, length = 50)
    private String pet;

    @Column(name = "procedure_name", nullable = false, length = 150)
    private String procedure;

    @Column(name = "state", nullable = false)
    private String state;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "is_authorized", nullable = false)
    private Boolean isAuthorized;

    public OrderDomain() {
    }

    @SuppressWarnings("java:S107")
    public OrderDomain(UUID id, String idOrder, String tutor, String pet, String procedure,
                       String state, LocalDateTime date, Boolean isAuthorized) {
        this.id = id;
        this.idOrder = idOrder;
        this.tutor = tutor;
        this.pet = pet;
        this.procedure = procedure;
        this.state = state;
        this.date = date;
        this.isAuthorized = isAuthorized;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(String idOrder) {
        this.idOrder = idOrder;
    }

    public String getTutor() {
        return tutor;
    }

    public void setTutor(String tutor) {
        this.tutor = tutor;
    }

    public String getPet() {
        return pet;
    }

    public void setPet(String pet) {
        this.pet = pet;
    }

    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Boolean getAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(Boolean authorized) {
        isAuthorized = authorized;
    }
}