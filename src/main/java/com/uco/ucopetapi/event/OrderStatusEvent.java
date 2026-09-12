package com.uco.ucopetapi.event;

import java.util.UUID;

public class OrderStatusEvent {

    private final UUID orderId;
    private final UUID personId;
    private final boolean authorized;

    public OrderStatusEvent(UUID orderId, UUID personId, boolean authorized) {
        this.orderId = orderId;
        this.personId = personId;
        this.authorized = authorized;
    }

    public UUID getOrderId() { return orderId; }
    public UUID getPersonId() { return personId; }
    public boolean isAuthorized() { return authorized; }
}
