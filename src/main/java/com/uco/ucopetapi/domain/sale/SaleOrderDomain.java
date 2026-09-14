package com.uco.ucopetapi.domain.sale;

import com.uco.ucopetapi.dto.sale.SaleOrderDTO;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "saleOrder")
public class SaleOrderDomain implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "headquarter_id", nullable = false)
    private UUID headquarterId;

    @Column(name = "order_number", nullable = false, unique = true, length = 20)
    private String orderNumber;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "client_id", nullable = false)
    private UUID clientID;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "health_plan_id")
    private UUID healthPlanId;

    @Column(name = "subtotal", nullable = false)
    private Integer subtotal;

    @Column(name = "total_discount", nullable = false)
    private Integer totalDiscount;

    @Column(name = "total_taxes", nullable = false)
    private Integer totalTaxes;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Column(name = "comment", length = 255)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private SaleOrderState state;

    @Transient
    private boolean isNew = true;

    protected SaleOrderDomain() {
        // Requerido por JPA/Hibernate para instanciar por reflexion al
        // leer filas de la base de datos. No usar directamente.
    }

    public SaleOrderDomain(UUID id, UUID headquarterId, String orderNumber, LocalDateTime saleDate, UUID clientID, UUID petId, UUID healthPlanId, Integer subtotal, Integer totalDiscount, Integer totalTaxes, Integer total, String comment, SaleOrderState state) {
        setID(id);
        setHeadquarterId(headquarterId);
        setOrderNumber(orderNumber);
        setSaleDate(saleDate);
        setClientID(clientID);
        setPetId(petId);
        setHealthPlanId(healthPlanId);
        setSubtotal(subtotal);
        setTotalDiscount(totalDiscount);
        setTotalTaxes(totalTaxes);
        setTotal(total);
        setComment(comment);
        setState(state);
        this.isNew = true;
    }

    public UUID getId() { return id; }
    public UUID getHeadquarterId() { return headquarterId; }
    public String getOrderNumber() { return orderNumber; }
    public LocalDateTime getSaleDate() { return saleDate; }
    public UUID getClientID() { return clientID; }
    public UUID getPetId() { return petId; }
    public UUID getHealthPlanId() { return healthPlanId; }
    public Integer getSubtotal() { return subtotal; }
    public Integer getTotalDiscount() { return totalDiscount; }
    public Integer getTotalTaxes() { return totalTaxes; }
    public Integer getTotal() { return total; }
    public String getComment() { return comment; }
    public SaleOrderState getState() { return state; }

    public void setID(UUID id) { this.id = id; }
    public void setHeadquarterId(UUID headquarterId) { this.headquarterId = headquarterId; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }
    public void setClientID(UUID clientID) { this.clientID = clientID; }
    public void setPetId(UUID petId) { this.petId = petId; }
    public void setHealthPlanId(UUID healthPlanId) { this.healthPlanId = healthPlanId; }
    public void setSubtotal(Integer subtotal) { this.subtotal = subtotal; }
    public void setTotalDiscount(Integer totalDiscount) { this.totalDiscount = totalDiscount; }
    public void setTotalTaxes(Integer totalTaxes) { this.totalTaxes = totalTaxes; }
    public void setTotal(Integer total) { this.total = total; }
    public void setComment(String comment) { this.comment = comment; }
    public void setState(SaleOrderState state) { this.state = state; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markAsNotNew() {
        this.isNew = false;
    }

    public SaleOrderDTO toDTO() {
        return new SaleOrderDTO(
                this.id,
                this.headquarterId,
                this.orderNumber,
                this.saleDate,
                this.clientID,
                this.petId,
                this.healthPlanId,
                this.subtotal,
                this.totalDiscount,
                this.totalTaxes,
                this.total,
                this.comment,
                this.state
        );
    }
}
