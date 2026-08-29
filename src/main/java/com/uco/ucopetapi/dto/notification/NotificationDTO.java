package com.uco.ucopetapi.dto.notification;

import com.uco.ucopetapi.model.notification.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationDTO {

    private UUID id;
    private UUID personId;
    private String title;
    private String message;
    private boolean isRead;
    private NotificationType type;
    private UUID referenceId;
    private String referenceType;
    private LocalDateTime createdAt;

    public NotificationDTO() {}

    public NotificationDTO(UUID id, UUID personId, String title, String message,
                           boolean isRead, NotificationType type,
                           UUID referenceId, String referenceType,
                           LocalDateTime createdAt) {
        this.id = id;
        this.personId = personId;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.type = type;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getPersonId() { return personId; }
    public void setPersonId(UUID personId) { this.personId = personId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public UUID getReferenceId() { return referenceId; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}