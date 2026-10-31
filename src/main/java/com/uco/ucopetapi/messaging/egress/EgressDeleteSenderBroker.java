package com.uco.ucopetapi.messaging.egress;

import com.uco.ucopetapi.dto.egress.EgressDTO;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EgressDeleteSenderBroker implements MessageSender<UUID> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;

    @Value("${payments.egress.delete.exchange-name}")
    private String exchangeName;

    @Value("${payments.egress.delete.routing-key-name}")
    private String routingKey;

    public EgressDeleteSenderBroker(final RabbitTemplate rabbitTemplate,
                                    final MapperJsonObject mapperJsonObject) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
    }

    @Override
    public void execute(final UUID egressId, final String idMessage) {
        final EgressDTO message = new EgressDTO();
        message.setId(egressId);

        final String json = mapperJsonObject.execute(message)
                .orElseThrow(() -> new IllegalStateException(
                        "No fue posible convertir el mensaje de eliminación a JSON"));

        rabbitTemplate.convertAndSend(exchangeName, routingKey, json, rabbitMessage -> {
            rabbitMessage.getMessageProperties().setMessageId(idMessage);
            rabbitMessage.getMessageProperties().setHeader("idMensaje", idMessage);
            return rabbitMessage;
        });
    }
}