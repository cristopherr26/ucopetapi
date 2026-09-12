package com.uco.ucopetapi.event;

import com.uco.ucopetapi.model.notification.enums.NotificationType;
import com.uco.ucopetapi.service.notification.NotificationService;
import org.springframework.context.event.EventListener;

public class NotificationEventListener {
    private final NotificationService notificationService;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener
    public void onAppointmentCreated(AppointmentCreatedEvent event) {
        notificationService.create(
                event.getPersonId(),
                "Cita confirmada",
                "Tu cita para " + event.getPetName() + " ha sido registrada exitosamente.",
                NotificationType.APPOINTMENT_CREATED,
                event.getAppointmentId(),
                "APPOINTMENT"
        );
    }

    @EventListener
    public void onOrderStatus(OrderStatusEvent event) {
        if (event.isAuthorized()) {
            notificationService.create(
                    event.getPersonId(),
                    "Orden autorizada",
                    "Tu orden ha sido aprobada y está en proceso.",
                    NotificationType.ORDER_AUTHORIZED,
                    event.getOrderId(),
                    "ORDER"
            );
        } else {
            notificationService.create(
                    event.getPersonId(),
                    "Orden rechazada",
                    "Tu orden no pudo ser aprobada. Contáctanos para más información.",
                    NotificationType.ORDER_REJECTED,
                    event.getOrderId(),
                    "ORDER"
            );
        }
    }
}
