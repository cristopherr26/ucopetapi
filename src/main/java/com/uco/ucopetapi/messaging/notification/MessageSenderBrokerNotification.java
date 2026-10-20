package com.uco.ucopetapi.messaging.notification;

import com.uco.ucopetapi.domain.notification.NotificationDomain;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class MessageSenderBrokerNotification {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper; // el de Spring, ya sabe manejar fechas

    @Value("${notification.create.exchange}")   private String createEx;
    @Value("${notification.create.routingkey}") private String createRk;
    @Value("${notification.delete.exchange}")   private String deleteEx;
    @Value("${notification.delete.routingkey}") private String deleteRk;

    public MessageSenderBrokerNotification(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendCreate(NotificationDomain notification) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", notification.getId());
        body.put("personId", notification.getPerson().getId());
        body.put("title", notification.getTitle());
        body.put("message", notification.getMessage());
        body.put("read", notification.isRead());
        body.put("type", notification.getType());
        body.put("referenceId", notification.getReferenceId());
        body.put("referenceType", notification.getReferenceType());
        body.put("createdAt", notification.getCreatedAt());
        rabbitTemplate.convertAndSend(createEx, createRk, toJson(body));
    }
    public void sendDelete(UUID id) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", id);
        rabbitTemplate.convertAndSend(deleteEx, deleteRk, toJson(body));
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo convertir a JSON", e);
        }
    }
}
