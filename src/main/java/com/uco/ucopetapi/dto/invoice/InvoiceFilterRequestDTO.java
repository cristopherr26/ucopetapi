package com.uco.ucopetapi.dto.invoice;

import com.uco.ucopetapi.dto.invoice.enums.InvoiceState;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

public class InvoiceFilterRequestDTO {

    private UUID headquarterId;
    private UUID clientID;
    private UUID petId;
    private UUID saleOrderId;
    private String invoiceNumber;
    private InvoiceState state;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateTo;

    public UUID getHeadquarterId() { return headquarterId; }
    public void setHeadquarterId(UUID headquarterId) { this.headquarterId = headquarterId; }

    public UUID getClientID() { return clientID; }
    public void setClientID(UUID clientID) { this.clientID = clientID; }

    public UUID getPetId() { return petId; }
    public void setPetId(UUID petId) { this.petId = petId; }

    public UUID getSaleOrderId() { return saleOrderId; }
    public void setSaleOrderId(UUID saleOrderId) { this.saleOrderId = saleOrderId; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public InvoiceState getState() { return state; }
    public void setState(InvoiceState state) { this.state = state; }

    public LocalDate getDateFrom() { return dateFrom; }
    public void setDateFrom(LocalDate dateFrom) { this.dateFrom = dateFrom; }

    public LocalDate getDateTo() { return dateTo; }
    public void setDateTo(LocalDate dateTo) { this.dateTo = dateTo; }
}
