package com.uco.ucopetapi.messaging.pet;

import com.uco.ucopetapi.config.pet.PetUpdateQueueConfig;
import com.uco.ucopetapi.domain.pet.PetDomain;
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
public class PetUpdateSenderBroker implements MessageSender<PetDomain> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final PetUpdateQueueConfig petUpdateQueueConfig;

    public PetUpdateSenderBroker(RabbitTemplate rabbitTemplate, MapperJsonObject mapperJsonObject,
                                 PetUpdateQueueConfig petUpdateQueueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.petUpdateQueueConfig = petUpdateQueueConfig;
    }

    @Override
    public void execute(PetDomain message, String idMessage) {
        MessageProperties messageProperties = generateMessageProperties(idMessage);

        Optional<Message> messageBody = getMessageBody(message, messageProperties);
        if (!messageBody.isPresent()) {
            return;
        }
        rabbitTemplate.convertAndSend(petUpdateQueueConfig.getExchangeName(), petUpdateQueueConfig.getRoutingKeyName(),
                messageBody.get());
    }

    private MessageProperties generateMessageProperties(String idMessageSender ) {
        return MessagePropertiesBuilder.newInstance()
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("idMensaje", idMessageSender)
                .build();
    }

    private Optional<Message> getMessageBody(Object message, MessageProperties messageProperties) {
        Optional<String> messageText = mapperJsonObject.execute(message);

        return messageText.map(msg -> MessageBuilder
                .withBody(msg.getBytes())
                .andProperties(messageProperties)
                .build());

    }
}
