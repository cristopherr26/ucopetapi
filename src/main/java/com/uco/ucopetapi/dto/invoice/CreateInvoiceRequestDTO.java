package com.uco.ucopetapi.dto.invoice;

import com.uco.ucopetapi.crosscutting.helpers.TextHelper;
import com.uco.ucopetapi.crosscutting.helpers.UUIDHelper;

import java.util.UUID;

public final class CreateInvoiceRequestDTO {

    private UUID saleOrderId;
    private String observations;

    public CreateInvoiceRequestDTO() {
        this.saleOrderId = UUIDHelper.getUUIDHelper().getDefault();
        this.observations = TextHelper.getDefault();
    }

    public CreateInvoiceRequestDTO(UUID saleOrderId, String observations) {
        setSaleOrderId(saleOrderId);
        setObservations(observations);
    }

    public UUID getSaleOrderId() { return saleOrderId; }
    public String getObservations() { return observations; }

    public void setSaleOrderId(UUID saleOrderId) {
        this.saleOrderId = UUIDHelper.getUUIDHelper().getDefault(saleOrderId);
    }
    public void setObservations(String observations) {
        this.observations = TextHelper.getDefault(observations);
    }
}
