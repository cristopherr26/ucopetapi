package com.uco.ucopetapi.dto.person;

public enum DocumentType {
    CC("Cedula de ciudadania"),
    TI("Tarjeta de identidad"),
    CE("Cedula de extranjeria"),
    PA("Pasaporte"),
    RC("Registro civil"),
    PPT("Permiso por proteccion temporal");

    private final String descripcion;

    DocumentType(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
