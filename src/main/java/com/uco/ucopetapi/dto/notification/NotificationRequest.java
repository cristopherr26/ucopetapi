package com.uco.ucopetapi.dto.notification;

import com.uco.ucopetapi.model.notification.enums.NotificationType;
import com.uco.ucopetapi.model.notification.enums.ReferenceType;

import java.util.UUID;

public class NotificationRequest {
    private UUID personId;
    private String title;
    private String message;
    private NotificationType type;
    private UUID referenceId;
    private ReferenceType referenceType;

    public NotificationRequest(UUID personId, String title, String message,
                               NotificationType type, UUID referenceId,
                               ReferenceType referenceType) {
        this.personId = personId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
    }

    public UUID getPersonId() {
        return personId;
    }

    public ReferenceType getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public NotificationType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }
}
