package com.uco.ucopetapi.controllers.notification;

import com.uco.ucopetapi.dto.notification.NotificationDTO;
import com.uco.ucopetapi.exception.notification.NotificationAccessDeniedException;
import com.uco.ucopetapi.service.notification.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<List<NotificationDTO>> getAllByPerson(@PathVariable UUID personId,
                                                                Authentication authentication) {
        requireOwner(personId, authentication);
        return ResponseEntity.ok(notificationService.getAllByPerson(personId));
    }

    @GetMapping("/person/{personId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnread(@PathVariable UUID personId,
                                                           Authentication authentication) {
        requireOwner(personId, authentication);
        return ResponseEntity.ok(notificationService.getUnreadByPerson(personId));
    }

    @GetMapping("/person/{personId}/unread/count")
    public ResponseEntity<Long> countUnread(@PathVariable UUID personId,
                                            Authentication authentication) {
        requireOwner(personId, authentication);
        return ResponseEntity.ok(notificationService.countUnreadByPerson(personId));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id, Authentication authentication) {
        notificationService.markAsRead(id, currentPersonId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/person/{personId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable UUID personId,
                                              Authentication authentication) {
        requireOwner(personId, authentication);
        notificationService.markAllAsRead(personId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        notificationService.delete(id, currentPersonId(authentication));
        return ResponseEntity.noContent().build();
    }

    private UUID currentPersonId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    private void requireOwner(UUID personId, Authentication authentication) {
        if (!currentPersonId(authentication).equals(personId)) {
            throw new NotificationAccessDeniedException();
        }
    }
}