package com.uco.ucopetapi.controllers.notification;

import com.uco.ucopetapi.dto.notification.NotificationDTO;
import com.uco.ucopetapi.model.notification.enums.NotificationType;
import com.uco.ucopetapi.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController (NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<List<NotificationDTO>> getAllByPerson(@PathVariable UUID personId) {
        return ResponseEntity.ok(notificationService.getAllByPerson(personId));
    }

    @GetMapping("/person/{personId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnread(@PathVariable UUID personId) {
        return ResponseEntity.ok(notificationService.getUnreadByPerson(personId));
    }

    @GetMapping("/person/{personId}/unread/count")
    public ResponseEntity<Long> countUnread(@PathVariable UUID personId) {
        return ResponseEntity.ok(notificationService.countUnreadByPerson(personId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/person/{personId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable UUID personId) {
        notificationService.markAllAsRead(personId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}