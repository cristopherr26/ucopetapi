package com.uco.ucopetapi.dto.petCare;

import java.util.UUID;

public class CreatePetCareRequestDTO {

    private PetCareRequestType type;
    private UUID productId;
    private UUID procedureId;
    private Integer quantity;
    private String notes;

    public CreatePetCareRequestDTO() {
    }

    public CreatePetCareRequestDTO(final PetCareRequestType type, final UUID productId, final UUID procedureId,
                                   final Integer quantity, final String notes) {
        this.type = type;
        this.productId = productId;
        this.procedureId = procedureId;
        this.quantity = quantity;
        this.notes = notes;
    }

    public PetCareRequestType getType() {
        return type;
    }

    public void setType(PetCareRequestType type) {
        this.type = type;
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(UUID procedureId) {
        this.procedureId = procedureId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

