package com.uco.ucopetapi.service.notification;

import com.uco.ucopetapi.domain.notificacion.NotificationDomain;
import com.uco.ucopetapi.dto.notification.NotificationDTO;
import com.uco.ucopetapi.repository.notification.INotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final INotificationRepository notificationRepository;

    public NotificationService(INotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
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