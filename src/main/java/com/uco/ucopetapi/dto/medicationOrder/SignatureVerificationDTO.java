package com.uco.ucopetapi.dto.medicationOrder;

import java.util.UUID;

public class SignatureVerificationDTO {

    private UUID orderId;
    private boolean signatureValid;

    public SignatureVerificationDTO() {
    }

    public SignatureVerificationDTO(final UUID orderId, final boolean signatureValid) {
        this.orderId = orderId;
        this.signatureValid = signatureValid;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public boolean isSignatureValid() {
        return signatureValid;
    }

    public void setSignatureValid(boolean signatureValid) {
        this.signatureValid = signatureValid;
    }
}