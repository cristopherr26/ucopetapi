package com.uco.ucopetapi.dto.invoice;

import com.uco.ucopetapi.crosscutting.helpers.DateTimeHelper;
import com.uco.ucopetapi.crosscutting.helpers.IntHelper;
import com.uco.ucopetapi.crosscutting.helpers.ObjectHelper;
import com.uco.ucopetapi.crosscutting.helpers.TextHelper;
import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;
import com.uco.ucopetapi.domain.invoice.InvoiceDomain;
import com.uco.ucopetapi.dto.invoice.enums.InvoiceState;

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
    private Integer total;
    private Integer paidAmount;
    private Integer balance;
    private String observations;
    private InvoiceState state;
    private static InvoiceDTO DEFAULT = new InvoiceDTO();

    public static InvoiceDTO getDefaultObject() {
        return DEFAULT;
    }

    public InvoiceDTO() {
        this.id = UUIDHelper.getUUIDHelper().getDefault();
        this.saleOrderId = UUIDHelper.getUUIDHelper().getDefault();
        this.invoiceNumber = TextHelper.getDefault();
        this.headquarterId = UUIDHelper.getUUIDHelper().getDefault();
        this.clientID = UUIDHelper.getUUIDHelper().getDefault();
        this.petId = UUIDHelper.getUUIDHelper().getDefault();
        this.issueDate = DateTimeHelper.getDefault();
        this.total = IntHelper.getDefault();
        this.paidAmount = IntHelper.getDefault();
        this.balance = IntHelper.getDefault();
        this.observations = TextHelper.getDefault();
        this.state = InvoiceState.SIN_ESTADO;
    }

    public InvoiceDTO(UUID id) {
        this();
        this.id = UUIDHelper.getUUIDHelper().getDefault(id);
    }

    public InvoiceDTO(UUID id, UUID saleOrderId, String invoiceNumber, UUID headquarterId, UUID clientID,
                       UUID petId, LocalDateTime issueDate, Integer total, Integer paidAmount, Integer balance,
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
    public Integer getTotal() { return total; }
    public Integer getPaidAmount() { return paidAmount; }
    public Integer getBalance() { return balance; }
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
    public void setTotal(Integer total) {
        this.total = IntHelper.getDefault(total);
    }
    public void setPaidAmount(Integer paidAmount) {
        this.paidAmount = IntHelper.getDefault(paidAmount);
    }
    public void setBalance(Integer balance) {
        this.balance = IntHelper.getDefault(balance);
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
