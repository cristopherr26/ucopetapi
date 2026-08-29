package com.uco.ucopetapi.dto.invoice;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class InvoiceDTO {

    private UUID id;
    private String invoiceNumber;
    private InvoiceType type;
    private UUID headquarterId;
    private UUID tutorId;
    private UUID petId;
    private UUID providerId;
    private LocalDateTime issueDate;
    private LocalDate dueDate;
    private Double totalAmount;
    private Double paidAmount;
    private Double balance;
    private InvoiceStatus status;
    private String observations;
    private List<InvoiceItemDTO> items;

    public InvoiceDTO() {
    }

    public InvoiceDTO(UUID id, String invoiceNumber, InvoiceType type, UUID headquarterId, UUID tutorId,
                      UUID petId, UUID providerId, LocalDateTime issueDate, LocalDate dueDate,
                      Double totalAmount, Double paidAmount, Double balance, InvoiceStatus status,
                      String observations, List<InvoiceItemDTO> items) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.type = type;
        this.headquarterId = headquarterId;
        this.tutorId = tutorId;
        this.petId = petId;
        this.providerId = providerId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.balance = balance;
        this.status = status;
        this.observations = observations;
        this.items = items;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public InvoiceType getType() {
        return type;
    }

    public void setType(InvoiceType type) {
        this.type = type;
    }

    public UUID getHeadquarterId() {
        return headquarterId;
    }

    public void setHeadquarterId(UUID headquarterId) {
        this.headquarterId = headquarterId;
    }

    public UUID getTutorId() {
        return tutorId;
    }

    public void setTutorId(UUID tutorId) {
        this.tutorId = tutorId;
    }

    public UUID getPetId() {
        return petId;
    }

    public void setPetId(UUID petId) {
        this.petId = petId;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDateTime issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public List<InvoiceItemDTO> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDTO> items) {
        this.items = items;
    }
}
