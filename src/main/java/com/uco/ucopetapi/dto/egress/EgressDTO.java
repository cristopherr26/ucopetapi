package com.uco.ucopetapi.dto.egress;

import java.time.LocalDate;
import java.util.UUID;

public class EgressDTO {

    private UUID id;
    private LocalDate date;
    private UUID provider;
    private UUID payMethod;
    private UUID purchaseOrder;
    private String concept;
    private Float total;

    public EgressDTO (){
    }
    public EgressDTO(UUID id, LocalDate date, UUID provider, UUID payMethod,
                     UUID purchaseOrder, String concept, Float total){
        this.id = id;
        this.date = date;
        this.provider = provider;
        this.payMethod = payMethod;
        this.purchaseOrder = purchaseOrder;
        this.concept = concept;
        this.total = total;
    }

    public UUID getId() {
        return id;
    }

    private void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    private void setDate(LocalDate date) {
        this.date = date;
    }

    public UUID getProvider() {
        return provider;
    }

    private void setProvider(UUID provider) {
        this.provider = provider;
    }

    public UUID getPayMethod() {
        return payMethod;
    }

    private void setPayMethod(UUID payMethod) {
        this.payMethod = payMethod;
    }

    public UUID getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(UUID purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(Float total) {
        this.total = total;
    }
}


