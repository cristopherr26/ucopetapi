package com.uco.ucopetapi.messaging.specialtieDoctor;

import com.uco.ucopetapi.config.specialtieDoctor.SpecialtieDoctorDeactivateQueueConfig;
import com.uco.ucopetapi.domain.specialtieDoctor.SpecialtieDoctorDomain;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Esta entidad no tiene campo "active": el listener debe interpretar
// este mensaje como "eliminar la relacion" (specialtieDoctorRepository.delete).
@Component
public class MessageSenderBrokerSpecialtieDoctorDeactivate implements MessageSender<SpecialtieDoctorDomain> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final SpecialtieDoctorDeactivateQueueConfig specialtieDoctorDeactivateQueueConfig;

    public MessageSenderBrokerSpecialtieDoctorDeactivate(RabbitTemplate rabbitTemplate,
                                                         MapperJsonObject mapperJsonObject,
                                                         SpecialtieDoctorDeactivateQueueConfig specialtieDoctorDeactivateQueueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.specialtieDoctorDeactivateQueueConfig = specialtieDoctorDeactivateQueueConfig;
    }

    @Override
    public void execute(SpecialtieDoctorDomain message, String idMessage) {
        MessageProperties messageProperties = buildMessageProperties(idMessage);

        Optional<Message> messageBody = buildMessageBody(message, messageProperties);
        if (messageBody.isEmpty()) {
            return;
        }
        rabbitTemplate.convertAndSend(
                specialtieDoctorDeactivateQueueConfig.getExchangeName(),
                specialtieDoctorDeactivateQueueConfig.getRoutingKeyName(),
                messageBody.get());
    }

    private MessageProperties buildMessageProperties(String idMessageSender) {
        return MessagePropertiesBuilder.newInstance()
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setHeader("idMensaje", idMessageSender)
                .build();
    }

    private Optional<Message> buildMessageBody(Object message, MessageProperties messageProperties) {
        Optional<String> messageText = mapperJsonObject.executeGson(message);

        return messageText.map(text -> MessageBuilder
                .withBody(text.getBytes())
                .andProperties(messageProperties)
                .build());
    }
}