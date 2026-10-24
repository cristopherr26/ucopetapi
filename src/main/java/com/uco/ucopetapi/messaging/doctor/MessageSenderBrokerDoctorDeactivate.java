package com.uco.ucopetapi.messaging.doctor;

import com.uco.ucopetapi.config.doctor.DoctorDeactivateQueueConfig;
import com.uco.ucopetapi.domain.doctor.DoctorDomain;
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
public class MessageSenderBrokerDoctorDeactivate implements MessageSender<DoctorDomain> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final DoctorDeactivateQueueConfig doctorDeactivateQueueConfig;

    public MessageSenderBrokerDoctorDeactivate(RabbitTemplate rabbitTemplate,
                                               MapperJsonObject mapperJsonObject,
                                               DoctorDeactivateQueueConfig doctorDeactivateQueueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.doctorDeactivateQueueConfig = doctorDeactivateQueueConfig;
    }

    @Override
    public void execute(DoctorDomain message, String idMessage) {
        MessageProperties messageProperties = buildMessageProperties(idMessage);

        Optional<Message> messageBody = buildMessageBody(message, messageProperties);
        if (messageBody.isEmpty()) {
            return;
        }
        rabbitTemplate.convertAndSend(
                doctorDeactivateQueueConfig.getExchangeName(),
                doctorDeactivateQueueConfig.getRoutingKeyName(),
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
