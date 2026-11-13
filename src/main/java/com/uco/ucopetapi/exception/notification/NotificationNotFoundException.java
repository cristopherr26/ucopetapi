package com.uco.ucopetapi.exception.notification;

public class NotificationNotFoundException extends RuntimeException {
    public NotificationNotFoundException() {
        super("Notificación no encontrada");
    }
}