package com.uco.ucopetapi.dto.invoice;

public class RegisterPaymentRequestDTO {

    private Double amount;

    public RegisterPaymentRequestDTO() {
    }

    public RegisterPaymentRequestDTO(Double amount) {
        this.amount = amount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
