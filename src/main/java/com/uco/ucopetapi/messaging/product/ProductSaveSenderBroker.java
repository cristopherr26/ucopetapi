package com.uco.ucopetapi.messaging.product;

import com.uco.ucopetapi.config.product.ProductSaveQueueConfig;
import com.uco.ucopetapi.dto.product.ProcessorReplyDTO;
import com.uco.ucopetapi.dto.product.ProductDTO;
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
public class ProductSaveSenderBroker {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final ProductSaveQueueConfig productSaveQueueConfig;

    public ProductSaveSenderBroker(RabbitTemplate rabbitTemplate, MapperJsonObject mapperJsonObject,
                             ProductSaveQueueConfig productSaveQueueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.productSaveQueueConfig = productSaveQueueConfig;
    }

    public Optional<ProcessorReplyDTO> executeAndWait(ProductDTO message, String idMessage) {
        MessageProperties messageProperties = generateMessageProperties(idMessage);
        SenderBrokerReply.setDeadline(messageProperties);
        Message messageBody = getMessageBody(message, messageProperties)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo convertir a JSON el mensaje para guardar el producto " + message.getId()));
        Message reply = rabbitTemplate.sendAndReceive(productSaveQueueConfig.getExchangeName(),
                productSaveQueueConfig.getRoutingKeyName(), messageBody);
        return Optional.ofNullable(reply).map(SenderBrokerReply::toReply);
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
