package com.uco.ucopetapi.messaging.attentionhealth;

import com.uco.ucopetapi.config.attentionhealth.AttentionHealthDeactivationQueueConfig;
import com.uco.ucopetapi.domain.attentionhealth.AttentionHealthDomain;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class AttentionHealthDeactivationSenderBroker implements MessageSender<AttentionHealthDomain> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final AttentionHealthDeactivationQueueConfig queueConfig;

    public AttentionHealthDeactivationSenderBroker(final RabbitTemplate rabbitTemplate,
                                                   final MapperJsonObject mapperJsonObject,
                                                   final AttentionHealthDeactivationQueueConfig queueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.queueConfig = queueConfig;
    }

    @Override
    public void execute(final AttentionHealthDomain message, final String idMessage) {
        MessageProperties properties = MessagePropertiesBuilder.newInstance()
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("idMensaje", idMessage)
                .build();

        Optional<Message> body = mapperJsonObject.executeGson(message)
                .map(json -> MessageBuilder.withBody(json.getBytes(StandardCharsets.UTF_8))
                        .andProperties(properties)
                        .build());

        if (body.isEmpty()) {
            throw new IllegalStateException("No se pudo convertir la atención a JSON.");
        }

        rabbitTemplate.convertAndSend(queueConfig.getExchangeName(), queueConfig.getRoutingKeyName(), body.get());
    }
}
