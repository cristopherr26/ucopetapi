package com.uco.ucopetapi.domain.egress;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
import com.uco.ucopetapi.domain.provider.ProviderDomain;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "egresses")
public class EgressDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "provider", nullable = false)
    private UUID provider;

    @Column(name = "payMethod", nullable = false)
    private UUID payMethod;

    @Column(name = "purchaseOrder", nullable = false)
    private UUID purchaseOrder;

    @Column(name = "concept", nullable = false)
    private String concept;

    @Column(name = "total", nullable = false)
    private Float total;

    public EgressDomain(){
    }

    public EgressDomain(UUID id, LocalDate date, UUID provider, UUID payMethod,
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

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public UUID getProvider() {
        return provider;
    }

    public void setProvider(UUID provider) {
        this.provider = provider;
    }

    public UUID getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(UUID payMethod) {
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