package com.uco.ucopetapi.controllers.notification;

import com.uco.ucopetapi.dto.notification.NotificationDTO;
import com.uco.ucopetapi.model.notification.enums.NotificationType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        List<NotificationDTO> notifications = List.of(
                new NotificationDTO(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Cita confirmada",
                        "Tu cita fue confirmada para mañana a las 10am",
                        false,
                        NotificationType.APPOINTMENT_CREATED,
                        UUID.randomUUID(),
                        "APPOINTMENT",
                        LocalDateTime.now()
                ),
                new NotificationDTO(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Pago recibido",
                        "Se registró el pago de tu factura #F-0042",
                        true,
                        NotificationType.PAYMENT_RECEIVED,
                        UUID.randomUUID(),
                        "INVOICE",
                        LocalDateTime.now().minusHours(3)
                )
        );
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> countUnread() {
        return ResponseEntity.ok(1L);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }
}