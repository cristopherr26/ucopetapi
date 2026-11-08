package com.uco.ucopetapi.messaging.transfer;

import com.uco.ucopetapi.config.transfer.TransferSaveQueueConfig;
import com.uco.ucopetapi.dto.transfers.TransferSaveMessageDTO;
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
public class TransferSaveSenderBroker implements MessageSender<TransferSaveMessageDTO> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final TransferSaveQueueConfig transferSaveQueueConfig;

    public TransferSaveSenderBroker(RabbitTemplate rabbitTemplate, MapperJsonObject mapperJsonObject,
                                    TransferSaveQueueConfig transferSaveQueueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.transferSaveQueueConfig = transferSaveQueueConfig;
    }

    @Override
    public void execute(TransferSaveMessageDTO message, String idMessage) {
        MessageProperties messageProperties = generateMessageProperties(idMessage);

        Message messageBody = getMessageBody(message, messageProperties)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo convertir a JSON el mensaje del traslado " + message.id()));

        rabbitTemplate.convertAndSend(transferSaveQueueConfig.getExchangeName(),
                transferSaveQueueConfig.getRoutingKeyName(), messageBody);
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