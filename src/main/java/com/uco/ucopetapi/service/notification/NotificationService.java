package com.uco.ucopetapi.service.notification;

import com.uco.ucopetapi.domain.notification.NotificationDomain;
import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.dto.notification.NotificationDTO;
import com.uco.ucopetapi.dto.notification.NotificationRequest;
import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.messaging.notification.MessageSenderBrokerNotification;
import com.uco.ucopetapi.repository.notification.INotificationRepository;
import com.uco.ucopetapi.repository.person.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final INotificationRepository notificationRepository;
    private final PersonRepository personRepository;
    private final MessageSenderBrokerNotification messageSender;
    public NotificationService(INotificationRepository notificationRepository,
                               PersonRepository personRepository,
                               MessageSenderBrokerNotification messageSender) {
        this.notificationRepository = notificationRepository;
        this.personRepository = personRepository;
        this.messageSender = messageSender;
    }

    public void create(NotificationRequest request) {
        PersonDomain person = personRepository.findById(request.getPersonId())
                .orElseThrow(() -> new BusinessException("La persona no existe"));
        NotificationDomain notification = NotificationDomain.create(person, request);
        notification.setId(UUID.randomUUID());
        notification.setCreatedAt(LocalDateTime.now(ZoneId.of("America/Bogota")));
        messageSender.sendCreate(notification);
    }

    public List<NotificationDTO> getAllByPerson(UUID personId) {
        return notificationRepository.findByPersonId(personId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<NotificationDTO> getUnreadByPerson(UUID personId) {
        return notificationRepository.findByPersonIdAndIsReadFalse(personId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public long countUnreadByPerson(UUID personId) {
        return notificationRepository.countByPersonIdAndIsReadFalse(personId);
    }

    @Transactional
    public void markAsRead(UUID notificationId) {
        NotificationDomain notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("Notificación no encontrada"));
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID personId) {
        notificationRepository.markAllAsReadByPersonId(personId);
    }


    public void delete(UUID notificationId) {
        messageSender.sendDelete(notificationId);
    }

    private NotificationDTO toDTO(NotificationDomain n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .personId(n.getPerson().getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.isRead())
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .createdAt(n.getCreatedAt())
                .build();
    }
}