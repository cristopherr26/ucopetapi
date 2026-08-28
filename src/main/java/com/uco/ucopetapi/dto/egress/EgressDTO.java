package com.uco.ucopetapi.dto.egress;

import java.time.LocalDate;
import java.util.UUID;

public class EgressDTO {

    private UUID id;
    private LocalDate date;
    private UUID provider;
    private UUID payMethod;
    private String product;
    private Integer quantity;
    private Float price;
    private Float totalPrice;

    public EgressDTO (){
    }
    public EgressDTO(UUID id, LocalDate date, UUID provider, UUID payMethod, String product,
                        Integer quantity, Float price, Float totalPrice){
        this.id = id;
        this.date = date;
        this.provider = provider;
        this.payMethod = payMethod;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.totalPrice = totalPrice;
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

    public String getProduct() {
        return product;
    }

    private void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    private void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Float getPrice() {
        return price;
    }

    private void setPrice(Float price) {
        this.price = price;
    }

    public Float getTotalPrice() {
        return totalPrice;
    }

    private void setTotalPrice(Float totalPrice) {
        this.totalPrice = totalPrice;
    }
}


