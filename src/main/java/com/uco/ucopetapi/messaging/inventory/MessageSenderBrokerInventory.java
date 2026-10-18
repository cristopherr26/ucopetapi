package com.uco.ucopetapi.messaging.inventory;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

import com.uco.ucopetapi.config.InventoryQueueProperties;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageSenderBrokerInventory implements MessageSender<InventoryCommand> {

    private static final String PRODUCER = "ucopetapi";
    private static final String ID_MESSAGE_HEADER = "idMessage";

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final InventoryQueueProperties queues;

    public MessageSenderBrokerInventory(RabbitTemplate rabbitTemplate, MapperJsonObject mapperJsonObject,
                                        InventoryQueueProperties queues) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.queues = queues;
    }

    @Override
    public void execute(InventoryCommand message, String idMessage) {
        InventoryQueueProperties.Queue queue = queues.forCommand(message.command());
        rabbitTemplate.convertAndSend(queue.exchangeName(), queue.routingKeyName(),
                buildMessage(envelope(message, idMessage), idMessage));
    }

    private static Map<String, Object> envelope(InventoryCommand message, String idMessage) {
        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("eventId", idMessage);
        envelope.put("type", "inventory." + message.command().replace('-', '.'));
        envelope.put("version", 1);
        envelope.put("occurredAt", OffsetDateTime.now(ZoneOffset.UTC).toString());
        envelope.put("correlationId", idMessage);
        envelope.put("producer", PRODUCER);
        envelope.put("data", message.data());
        return envelope;
    }

    private Message buildMessage(Map<String, Object> envelope, String idMessage) {
        String body = mapperJsonObject.execute(envelope)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo convertir a JSON el comando " + envelope.get("type")));
        MessageProperties properties = MessagePropertiesBuilder.newInstance()
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.name())
                .setHeader(ID_MESSAGE_HEADER, idMessage)
                .build();
        return MessageBuilder.withBody(body.getBytes(StandardCharsets.UTF_8)).andProperties(properties).build();
    }
}
