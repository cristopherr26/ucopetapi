package com.uco.ucopetapi.dto.medicationOrder;

import java.util.ArrayList;
import java.util.List;

public class CreateMedicationOrderRequest {
    private List<MedicationOrderItemRequest> items = new ArrayList<>();
    private String notes;

    public CreateMedicationOrderRequest() {
    }

    public CreateMedicationOrderRequest(final List<MedicationOrderItemRequest> items, final String notes) {
        this.items = items != null ? items : new ArrayList<>();
        this.notes = notes;
    }

    public List<MedicationOrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<MedicationOrderItemRequest> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
