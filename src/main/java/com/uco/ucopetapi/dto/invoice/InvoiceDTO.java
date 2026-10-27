package com.uco.ucopetapi.dto.invoice;

import com.uco.ucopetapi.crosscutting.helpers.DateTimeHelper;
import com.uco.ucopetapi.crosscutting.helpers.ObjectHelper;
import com.uco.ucopetapi.crosscutting.helpers.TextHelper;
import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;
import com.uco.ucopetapi.domain.invoice.InvoiceDomain;
import com.uco.ucopetapi.dto.invoice.enums.InvoiceState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class InvoiceDTO {
    private UUID id;
    private UUID saleOrderId;
    private String invoiceNumber;
    private UUID headquarterId;
    private UUID clientID;
    private UUID petId;
    private LocalDateTime issueDate;
    private BigDecimal total;
    private BigDecimal paidAmount;
    private BigDecimal balance;
    private String observations;
    private InvoiceState state;

    public static InvoiceDTO getDefaultObject() {
        return new InvoiceDTO();
    }

    public InvoiceDTO() {
        this.id = UUIDHelper.getUUIDHelper().getDefault();
        this.saleOrderId = UUIDHelper.getUUIDHelper().getDefault();
        this.invoiceNumber = TextHelper.getDefault();
        this.headquarterId = UUIDHelper.getUUIDHelper().getDefault();
        this.clientID = UUIDHelper.getUUIDHelper().getDefault();
        this.petId = UUIDHelper.getUUIDHelper().getDefault();
        this.issueDate = DateTimeHelper.getDefault();
        this.total = BigDecimal.ZERO;
        this.paidAmount = BigDecimal.ZERO;
        this.balance = BigDecimal.ZERO;
        this.observations = TextHelper.getDefault();
        this.state = InvoiceState.SIN_ESTADO;
    }

    public InvoiceDTO(UUID id) {
        this();
        this.id = UUIDHelper.getUUIDHelper().getDefault(id);
    }

    public InvoiceDTO(UUID id, UUID saleOrderId, String invoiceNumber, UUID headquarterId, UUID clientID,
                      UUID petId, LocalDateTime issueDate, BigDecimal total, BigDecimal paidAmount, BigDecimal balance,
                      String observations, InvoiceState state) {
        this.id = UUIDHelper.getUUIDHelper().getDefault(id);
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
    }

    public UUID getId() { return id; }
    public UUID getSaleOrderId() { return saleOrderId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public UUID getHeadquarterId() { return headquarterId; }
    public UUID getClientID() { return clientID; }
    public UUID getPetId() { return petId; }
    public LocalDateTime getIssueDate() { return issueDate; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public BigDecimal getBalance() { return balance; }
    public String getObservations() { return observations; }
    public InvoiceState getState() { return state; }

    public void setId(UUID id) {
        this.id = UUIDHelper.getUUIDHelper().getDefault(id);
    }
    public void setSaleOrderId(UUID saleOrderId) {
        this.saleOrderId = UUIDHelper.getUUIDHelper().getDefault(saleOrderId);
    }
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = TextHelper.getDefault(invoiceNumber);
    }
    public void setHeadquarterId(UUID headquarterId) {
        this.headquarterId = UUIDHelper.getUUIDHelper().getDefault(headquarterId);
    }
    public void setClientID(UUID clientID) {
        this.clientID = UUIDHelper.getUUIDHelper().getDefault(clientID);
    }
    public void setPetId(UUID petId) {
        this.petId = UUIDHelper.getUUIDHelper().getDefault(petId);
    }
    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = DateTimeHelper.getDefault(issueDate);
    }
    public void setTotal(BigDecimal total) {
        this.total = ObjectHelper.getDefault(total, BigDecimal.ZERO);
    }
    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = ObjectHelper.getDefault(paidAmount, BigDecimal.ZERO);
    }
    public void setBalance(BigDecimal balance) {
        this.balance = ObjectHelper.getDefault(balance, BigDecimal.ZERO);
    }
    public void setObservations(String observations) {
        this.observations = TextHelper.getDefault(observations);
    }
    public void setState(InvoiceState state) {
        this.state = ObjectHelper.getDefault(state, InvoiceState.SIN_ESTADO);
    }

    public InvoiceDomain toDomain() {
        if (this.state == InvoiceState.SIN_ESTADO) {
            throw new IllegalStateException(
                    "No se puede persistir una Invoice sin un estado de negocio definido.");
        }

        return new InvoiceDomain(
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