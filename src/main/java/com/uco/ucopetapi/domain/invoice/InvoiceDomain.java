package com.uco.ucopetapi.domain.invoice;

import com.uco.ucopetapi.dto.invoice.InvoiceDTO;
import com.uco.ucopetapi.dto.invoice.enums.InvoiceState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invoice",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invoice_sale_order",
                columnNames = {"sale_order_id"}
        ))
public class InvoiceDomain implements Persistable<UUID> {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "sale_order_id", nullable = false, updatable = false)
    private UUID saleOrderId;

    @Column(name = "invoice_number", nullable = false, length = 20)
    private String invoiceNumber;

    @Column(name = "headquarter_id", nullable = false)
    private UUID headquarterId;

    @Column(name = "client_id", nullable = false)
    private UUID clientID;

    @Column(name = "pet_id", nullable = false)
    private UUID petId;

    @Column(name = "issue_date", nullable = false)
    private LocalDateTime issueDate;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Column(name = "paid_amount", nullable = false)
    private Integer paidAmount;

    @Column(name = "balance", nullable = false)
    private Integer balance;

    @Column(name = "observations", length = 255)
    private String observations;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 25)
    private InvoiceState state;

    @Transient
    private boolean isNew = true;

    protected InvoiceDomain() {
        // Requerido por JPA/Hibernate para instanciar por reflexion al
        // leer filas de la base de datos. No usar directamente.
    }

    public InvoiceDomain(UUID id, UUID saleOrderId, String invoiceNumber, UUID headquarterId, UUID clientID,
                          UUID petId, LocalDateTime issueDate, Integer total, Integer paidAmount, Integer balance,
                          String observations, InvoiceState state) {
        setID(id);
        setSaleOrderId(saleOrderId);
        setInvoiceNumber(invoiceNumber);
        setHeadquarterId(headquarterId);
        setClientID(clientID);
        setPetId(petId);
        setIssueDate(issueDate);
        setTotal(total);
        setPaidAmount(paidAmount);
        setBalance(balance);
        setObservations(observations);
        setState(state);
        this.isNew = true;
    }

    public UUID getId() { return id; }
    public UUID getSaleOrderId() { return saleOrderId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public UUID getHeadquarterId() { return headquarterId; }
    public UUID getClientID() { return clientID; }
    public UUID getPetId() { return petId; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public Integer getTotal() { return total; }
    public Integer getPaidAmount() { return paidAmount; }
    public Integer getBalance() { return balance; }
    public String getObservations() { return observations; }
    public InvoiceState getState() { return state; }

    public void setID(UUID id) { this.id = id; }
    public void setSaleOrderId(UUID saleOrderId) { this.saleOrderId = saleOrderId; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public void setHeadquarterId(UUID headquarterId) { this.headquarterId = headquarterId; }
    public void setClientID(UUID clientID) { this.clientID = clientID; }
    public void setPetId(UUID petId) { this.petId = petId; }
    public void setIssueDate(LocalDateTime issueDate) { this.issueDate = issueDate; }
    public void setTotal(Integer total) { this.total = total; }
    public void setPaidAmount(Integer paidAmount) { this.paidAmount = paidAmount; }
    public void setBalance(Integer balance) { this.balance = balance; }
    public void setObservations(String observations) { this.observations = observations; }
    public void setState(InvoiceState state) { this.state = state; }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markAsNotNew() {
        this.isNew = false;
    }

    public InvoiceDTO toDTO() {
        return new InvoiceDTO(
                this.id,
                this.saleOrderId,
                this.invoiceNumber,
                this.headquarterId,
                this.clientID,
                this.petId,
                this.issueDate,
                this.total,
                this.paidAmount,
                this.balance,
                this.observations,
                this.state
        );
    }
}
