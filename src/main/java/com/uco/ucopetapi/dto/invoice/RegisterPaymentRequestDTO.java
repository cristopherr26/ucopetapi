package com.uco.ucopetapi.dto.invoice;

import java.math.BigDecimal;

public final class RegisterPaymentRequestDTO {

    private BigDecimal amount;

    public RegisterPaymentRequestDTO() {
    }

    public RegisterPaymentRequestDTO(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}