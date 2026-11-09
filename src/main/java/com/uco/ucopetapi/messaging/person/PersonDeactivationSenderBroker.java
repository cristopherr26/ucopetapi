package com.uco.ucopetapi.messaging.person;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.uco.ucopetapi.config.person.PersonDeactivationQueueConfig;
import com.uco.ucopetapi.dto.person.PersonMessageDTO;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;

@Component
public class PersonDeactivationSenderBroker implements MessageSender<PersonMessageDTO> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final PersonDeactivationQueueConfig personDeactivationQueueConfig;

    public PersonDeactivationSenderBroker(RabbitTemplate rabbitTemplate, MapperJsonObject mapperJsonObject,
                                          PersonDeactivationQueueConfig personDeactivationQueueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.personDeactivationQueueConfig = personDeactivationQueueConfig;
    }

    @Override
    public void execute(PersonMessageDTO message, String idMessage) {
        MessageProperties messageProperties = generateMessageProperties(idMessage);

        Message messageBody = getMessageBody(message, messageProperties)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo convertir a JSON el mensaje de la persona " + message.id()));

        rabbitTemplate.convertAndSend(personDeactivationQueueConfig.getExchangeName(),
                personDeactivationQueueConfig.getRoutingKeyName(), messageBody);
    }

    private MessageProperties generateMessageProperties(String idMessageSender) {
        return MessagePropertiesBuilder.newInstance()
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("idMensaje", idMessageSender)
                .build();
    }

    private Optional<Message> getMessageBody(Object message, MessageProperties messageProperties) {
        Optional<String> messageText = mapperJsonObject.execute(message);

        return messageText.map(msg -> MessageBuilder
                .withBody(msg.getBytes(StandardCharsets.UTF_8))
                .andProperties(messageProperties)
                .build());
    }
}