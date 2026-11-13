package com.uco.ucopetapi.messaging.tutorPet;

import com.uco.ucopetapi.config.tutorPet.TutorPetUpdateQueueConfig;
import com.uco.ucopetapi.dto.tutorPet.TutorPetDTO;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TutorPetUpdateSenderBroker implements MessageSender<TutorPetDTO> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final TutorPetUpdateQueueConfig queueConfig;

    public TutorPetUpdateSenderBroker(RabbitTemplate rabbitTemplate, MapperJsonObject mapperJsonObject,
                                         TutorPetUpdateQueueConfig queueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.queueConfig = queueConfig;
    }

    @Override
    public void execute(TutorPetDTO message, String idMessage) {
        MessageProperties properties = MessagePropertiesBuilder.newInstance()
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("idMensaje", idMessage)
                .build();

        Optional<Message> body = mapperJsonObject.execute(message)
                .map(json -> MessageBuilder.withBody(json.getBytes())
                        .andProperties(properties)
                        .build());

        body.ifPresent(rabbitMessage -> rabbitTemplate.convertAndSend(
                queueConfig.getExchangeName(), queueConfig.getRoutingKeyName(), rabbitMessage));
    }
}