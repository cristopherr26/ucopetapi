package com.uco.ucopetapi.exception.notification;

public class NotificationAccessDeniedException extends RuntimeException {
    public NotificationAccessDeniedException() {
        super("No puedes acceder a las notificaciones de otra persona");
    }
}
