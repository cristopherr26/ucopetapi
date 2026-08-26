package com.uco.ucopetapi.dto.order;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderDTO {

    private UUID id;
    private String idOrden;
    private String tutor;
    private String mascota;
    private String procedimiento;
    private String estado;
    private LocalDateTime fecha;
    private boolean isAuthorized;


    public OrderDTO(){
    }

    public OrderDTO(UUID id, String idOrden, String tutor, String mascota, String procedimiento, String estado,
                    LocalDateTime fecha, boolean isAuthorized) {
        this.id = id;
        this.idOrden = idOrden;
        this.tutor = tutor;
        this.mascota = mascota;
        this.procedimiento = procedimiento;
        this.estado = estado;
        this.fecha = fecha;
        this.isAuthorized = isAuthorized;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIdOrden() {
        return idOrden;
    }

    public void setIdOrden(String idOrden) {
        this.idOrden = idOrden;
    }

    public String getTutor() {
        return tutor;
    }

    public void setTutor(String tutor) {
        this.tutor = tutor;
    }

    public String getMascota() {
        return mascota;
    }

    public void setMascota(String mascota) {
        this.mascota = mascota;
    }

    public String getProcedimiento() {
        return procedimiento;
    }

    public void setProcedimiento(String procedimiento) {
        this.procedimiento = procedimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean authorized) {
        isAuthorized = authorized;
    }
}
