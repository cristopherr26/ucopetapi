package com.uco.ucopetapi.messaging.spaces;

import com.uco.ucopetapi.config.spaces.SpaceCreationQueueConfig;
import com.uco.ucopetapi.domain.space.SpaceDomain;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;


//Publicador de Space para crear y actualizar,
//El mensaje va de la Api a Rabbit: no espera a que lo procesen es asincrono.

@Component
public class MessageSenderBrokerCreationSpace implements MessageSender<SpaceDomain> {

    private static final Logger log = LoggerFactory.getLogger(MessageSenderBrokerCreationSpace.class);

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final SpaceCreationQueueConfig queueConfig;

    public MessageSenderBrokerCreationSpace(RabbitTemplate rabbitTemplate,
                                            MapperJsonObject mapperJsonObject,
                                            SpaceCreationQueueConfig queueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.queueConfig = queueConfig;
    }

    @Override
    public void execute(SpaceDomain message, String idMessage) {
        MessageProperties messageProperties = generateMessageProperties(idMessage);

        Optional<Message> messageBody = getMessageBody(message, messageProperties);
        if (messageBody.isEmpty()) {
            throw new IllegalStateException("No se pudo convertir el espacio a JSON.");
        }

        rabbitTemplate.convertAndSend(queueConfig.getExchangeName(), queueConfig.getRoutingKeyName(), messageBody.get());
        log.info("[SPACE-PUBLISH] crear y actualizar -> idMensaje={} exchange={}", idMessage, queueConfig.getExchangeName());
    }

    private MessageProperties generateMessageProperties(String idMessage) {
        return MessagePropertiesBuilder.newInstance()
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("idMensaje", idMessage)
                .build();
    }

    private Optional<Message> getMessageBody(SpaceDomain message, MessageProperties messageProperties) {
        return mapperJsonObject.executeGson(message)
                .map(json -> MessageBuilder
                        .withBody(json.getBytes(StandardCharsets.UTF_8))
                        .andProperties(messageProperties)
                        .build());
    }
}

