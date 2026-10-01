package com.uco.ucopetapi.dto.invoice;

import com.uco.ucopetapi.crosscutting.helpers.IntHelper;

public final class RegisterPaymentRequestDTO {

    private Integer amount;

    public RegisterPaymentRequestDTO() {
        this.amount = IntHelper.getDefault();
    }

    public RegisterPaymentRequestDTO(Integer amount) {
        setAmount(amount);
    }

    public Integer getAmount() { return amount; }

    public void setAmount(Integer amount) {
        this.amount = IntHelper.getDefault(amount);
    }
}
