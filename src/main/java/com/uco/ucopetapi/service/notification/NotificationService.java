package com.uco.ucopetapi.service.notification;

import com.uco.ucopetapi.domain.notificacion.NotificationDomain;
import com.uco.ucopetapi.domain.person.PersonDomain;
import com.uco.ucopetapi.dto.notification.NotificationDTO;
import com.uco.ucopetapi.model.notification.enums.NotificationType;
import com.uco.ucopetapi.repository.notification.INotificationRepository;
import com.uco.ucopetapi.repository.person.PersonRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final INotificationRepository notificationRepository;
    private final PersonRepository personRepository;

    public NotificationService(INotificationRepository notificationRepository, PersonRepository personRepository) {
        this.notificationRepository = notificationRepository;
        this.personRepository = personRepository;
    }

    public void create(UUID personId, String title, String message,
                       NotificationType type, UUID referenceId, String referenceType) {

        PersonDomain person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found: " + personId));

        NotificationDomain notification = new NotificationDomain();
        notification.setPerson(person);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);

        notificationRepository.save(notification);
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
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(UUID personId) {
        notificationRepository.markAllAsReadByPersonId(personId);
    }

    public void delete(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    private NotificationDTO toDTO(NotificationDomain n) {
        return new NotificationDTO(
                n.getId(),
                n.getPerson().getId(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getType(),
                n.getReferenceId(),
                n.getReferenceType(),
                n.getCreatedAt()
        );
    }
}