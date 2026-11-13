package com.uco.ucopetapi.messaging.transfer;

import com.uco.ucopetapi.config.transfer.TransferCancelQueueConfig;
import com.uco.ucopetapi.dto.transfers.TransferCancelMessageDTO;
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
public class TransferCancelSenderBroker implements MessageSender<TransferCancelMessageDTO> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final TransferCancelQueueConfig transferCancelQueueConfig;

    public TransferCancelSenderBroker(RabbitTemplate rabbitTemplate, MapperJsonObject mapperJsonObject,
                                      TransferCancelQueueConfig transferCancelQueueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.transferCancelQueueConfig = transferCancelQueueConfig;
    }

    @Override
    public void execute(TransferCancelMessageDTO message, String idMessage) {
        MessageProperties messageProperties = generateMessageProperties(idMessage);

        Message messageBody = getMessageBody(message, messageProperties)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo convertir a JSON el mensaje para cancelar el traslado " + message.id()));

        rabbitTemplate.convertAndSend(transferCancelQueueConfig.getExchangeName(),
                transferCancelQueueConfig.getRoutingKeyName(), messageBody);
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