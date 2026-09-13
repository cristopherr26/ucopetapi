package com.uco.ucopetapi.event;

import com.uco.ucopetapi.model.notification.enums.NotificationType;
import com.uco.ucopetapi.service.notification.NotificationService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
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

        notificationService.create(
                event.getPersonId(),
                "Cambio estado en la orden",
                event.isAuthorized()
                        ? "Tu orden fue aprobada y está en proceso."
                        : "Tu orden no pudo ser aprobada. Contáctanos.",
                event.isAuthorized() ? NotificationType.ORDER_AUTHORIZED : NotificationType.ORDER_REJECTED,
                event.getOrderId(),
                "ORDER"
        );
    }

    @EventListener
    public void onLoginSucceded(LoginSuccededEvent event) {
        notificationService.create(
                event.getPersonId(),
                "Inicio de sesion exitoso",
                "Ultimo inicio de sesion exitoso: " + event.getDate() + ".",
                NotificationType.LOGIN_SUCCEDED,
                event.getLoginId(),
                "LOGIN"
        );
    }
}
