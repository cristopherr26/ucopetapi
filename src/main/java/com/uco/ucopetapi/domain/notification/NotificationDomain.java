package com.uco.ucopetapi.domain.notification;

import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.dto.notification.NotificationRequest;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.model.notification.enums.NotificationType;
import com.uco.ucopetapi.model.notification.enums.ReferenceType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class NotificationDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private PersonDomain person;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type")
    private ReferenceType referenceType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now(ZoneId.of("America/Bogota"));
    }

    // Business methods
    public void markAsRead() {
        this.isRead = true;
    }

    public boolean isUnread() {
        return !this.isRead;
    }

    // Getters & Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public PersonDomain getPerson() { return person; }
    public void setPerson(PersonDomain person) { this.person = person; }

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

    public ReferenceType getReferenceType() { return referenceType; }
    public void setReferenceType(ReferenceType referenceType) { this.referenceType = referenceType; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static NotificationDomain create(PersonDomain person, NotificationRequest request) {
        NotificationDomain notification = new NotificationDomain();
        notification.person = person;
        notification.title = request.getTitle();
        notification.message = request.getMessage();
        notification.type = request.getType();
        notification.referenceId = request.getReferenceId();
        notification.referenceType = request.getReferenceType();
        notification.isRead = false;          // una notificación nueva siempre nace sin leer
        notification.validate();              // si algo está mal, aquí se lanza el error
        return  notification;
    }

    public void validate() {
        if (person == null)
            throw new BusinessException("El destinatario es obligatorio");

        if (title == null || title.isBlank())
            throw new BusinessException("El título es obligatorio");
        if (title.length() > 150)
            throw new BusinessException("El título no puede superar 150 caracteres");

        if (message == null || message.isBlank())
            throw new BusinessException("El mensaje es obligatorio");
        if (message.length() > 255)
            throw new BusinessException("El mensaje no puede superar 500 caracteres");

        if (type == null)
            throw new BusinessException("El tipo de notificación es obligatorio");

        // referencia: o vienen las dos o no viene ninguna
        if ((referenceId == null) != (referenceType == null))
            throw new BusinessException("referenceId y referenceType deben venir juntos");

        // el tipo de notificación debe coincidir con su referencia
        if (referenceType != null && referenceType != expectedReference(type))
            throw new BusinessException("El tipo de referencia no corresponde al tipo de notificación");
    }

    private static ReferenceType expectedReference(NotificationType type) {
        return switch (type) {
            case APPOINTMENT_CREATED -> ReferenceType.APPOINTMENT;
            case ORDER_AUTHORIZED, ORDER_REJECTED, ORDER_PENDING -> ReferenceType.ORDER;
            case LOGIN_SUCCEDED -> ReferenceType.LOGIN;
        };
    }
}