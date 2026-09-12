package com.uco.ucopetapi.repository.notification;

import com.uco.ucopetapi.domain.notificacion.NotificationDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface INotificationRepository extends JpaRepository<NotificationDomain, UUID> {

    List<NotificationDomain> findByPersonId(UUID personId);

    List<NotificationDomain> findByPersonIdAndIsReadFalse(UUID personId);

    long countByPersonIdAndIsReadFalse(UUID personId);

    @Modifying
    @Query("UPDATE NotificationDomain n SET n.isRead = true " +
            "WHERE n.person.id = :personId AND n.isRead = false")
    void markAllAsReadByPersonId(@Param("personId") UUID personId);
}