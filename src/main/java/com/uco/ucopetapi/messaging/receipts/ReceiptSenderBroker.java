package com.uco.ucopetapi.messaging.receipts;

import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public abstract class ReceiptSenderBroker<T> implements MessageSender<T> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final String exchangeName;
    private final String routingKey;

    protected ReceiptSenderBroker(final RabbitTemplate rabbitTemplate,
                                  final MapperJsonObject mapperJsonObject,
                                  final String exchangeName,
                                  final String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public void execute(final T message, final String idMessage) {
        final String json = mapperJsonObject.execute(message)
                .orElseThrow(() -> new IllegalStateException(
                        "No fue posible serializar el mensaje del recibo"));
        rabbitTemplate.convertAndSend(exchangeName, routingKey, json, rabbitMessage -> {
            rabbitMessage.getMessageProperties().setMessageId(idMessage);
            return rabbitMessage;
        });
    }
}