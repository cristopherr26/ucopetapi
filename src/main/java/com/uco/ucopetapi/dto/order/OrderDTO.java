package com.uco.ucopetapi.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderDTO {

    private UUID id;

    @NotBlank(message = "El identificador de la orden no puede estar vacío")
    @Size(min = 5, max = 20, message = "El idOrder debe tener entre 5 y 20 caracteres")
    @Pattern(regexp = "^ORD-\\d{4}-\\d{3}$", message = "El idOrder debe tener el formato ORD-YYYY-XXX")
    private String idOrder;

    @NotBlank(message = "El nombre del tutor es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre del tutor debe tener entre 3 y 100 caracteres")
    private String tutor;

    @NotBlank(message = "El nombre de la mascota es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre de la mascota debe tener entre 2 y 50 caracteres")
    private String pet;

    @NotBlank(message = "El procedimiento es obligatorio")
    @Size(max = 150, message = "El procedimiento no puede exceder 150 caracteres")
    private String procedure;

    @NotBlank(message = "El estado es obligatorio")
    private String state;

    private LocalDateTime date;

    @NotNull(message = "El campo de autorización es obligatorio")
    private Boolean isAuthorized;

    public OrderDTO() {
    }

    @SuppressWarnings("java:S107")
    public OrderDTO(UUID id, String idOrder, String tutor, String pet, String procedure, String state,
                    LocalDateTime date, Boolean isAuthorized) {
        this.id = id;
        this.idOrder = idOrder;
        this.tutor = tutor;
        this.pet = pet;
        this.procedure = procedure;
        this.state = state;
        this.date = date;
        this.isAuthorized = isAuthorized;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getIdOrder() { return idOrder; }
    public void setIdOrder(String idOrder) { this.idOrder = idOrder; }

    public String getTutor() { return tutor; }
    public void setTutor(String tutor) { this.tutor = tutor; }

    public String getPet() { return pet; }
    public void setPet(String pet) { this.pet = pet; }

    public String getProcedure() { return procedure; }
    public void setProcedure(String procedure) { this.procedure = procedure; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public Boolean getAuthorized() { return isAuthorized; }
    public void setAuthorized(Boolean authorized) { isAuthorized = authorized; }
}