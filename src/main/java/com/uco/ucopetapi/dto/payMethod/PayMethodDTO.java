package com.uco.ucopetapi.dto.payMethod;

import java.util.UUID;

public class PayMethodDTO {
    private UUID id;
    private String name;

    public PayMethodDTO(){
    }
    public PayMethodDTO (UUID id, String name){
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    private void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }
}

