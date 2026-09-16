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

    public NotificationDTO() { // Se usa patron builder para instanciar la clase

        }

    public static Builder builder() {
        return new Builder();
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

    public static class Builder {
        private final NotificationDTO dto = new NotificationDTO();

        public Builder id(UUID id) {
            dto.id = id;
            return this;
        }

        public Builder personId(UUID personId) {
            dto.personId = personId;
            return this;
        }

        public Builder title(String title) {
            dto.title = title;
            return this;
        }

        public Builder message(String message) {
            dto.message = message;
            return this;
        }

        public Builder isRead(boolean read) {
            dto.isRead = read;
            return this;
        }

        public Builder type(NotificationType type) {
            dto.type = type;
            return this;
        }

        public Builder referenceId(UUID referenceId) {
            dto.referenceId = referenceId;
            return this;
        }

        public Builder referenceType(String referenceType) {
            dto.referenceType = referenceType;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            dto.createdAt = createdAt;
            return this;
        }

        public NotificationDTO build() {
            return dto;
        }
    }
}