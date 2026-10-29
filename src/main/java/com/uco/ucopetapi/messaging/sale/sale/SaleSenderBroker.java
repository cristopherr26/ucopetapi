package com.uco.ucopetapi.messaging.sale.sale;

import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public abstract class SaleSenderBroker<T> implements MessageSender<T> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final String exchangeName;
    private final String routingKey;

    protected SaleSenderBroker(
            RabbitTemplate rabbitTemplate,
            MapperJsonObject mapperJsonObject,
            String exchangeName,
            String routingKey) {

        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.exchangeName = exchangeName;
        this.routingKey = routingKey;
    }

    @Override
    public void execute(T message, String idMessage) {

        String json = mapperJsonObject.execute(message)
                .orElseThrow(() -> new IllegalStateException(
                        "No fue posible serializar el mensaje de la venta"));

        rabbitTemplate.convertAndSend(
                exchangeName,
                routingKey,
                json,
                rabbitMessage -> {
                    rabbitMessage.getMessageProperties()
                            .setMessageId(idMessage);
                    return rabbitMessage;
                }
        );
    }
}
