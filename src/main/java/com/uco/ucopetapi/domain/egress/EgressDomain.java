package com.uco.ucopetapi.domain.egress;

import com.uco.ucopetapi.domain.payMethod.PayMethodDomain;
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

    /*@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderDomain provider;*/

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paymethod_id", nullable = false)
    private PayMethodDomain payMethod;

    @Column(name = "product", nullable = false)
    private String product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false)
    private Float price;

    @Column(name = "totalPrice", nullable = false)
    private Float totalPrice;

    public EgressDomain(){
    }

    public EgressDomain(UUID id, LocalDate date, /*ProviderDomain provider,*/ PayMethodDomain payMethod,
                        String product, Integer quantity, Float price, Float totalPrice){

        this.id = id;
        this.date = date;
        //this.provider = provider;
        this.payMethod = payMethod;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
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

    /*public ProviderDomain getProvider() {
        return provider;
    }

    public void setProvider(ProviderDomain provider) {
        this.provider = provider;
    }*/

    public PayMethodDomain getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(PayMethodDomain payMethod) {
        this.payMethod = payMethod;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public Float getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Float totalPrice) {
        this.totalPrice = totalPrice;
    }
}