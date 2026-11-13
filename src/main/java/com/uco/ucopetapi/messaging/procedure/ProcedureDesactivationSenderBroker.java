package com.uco.ucopetapi.messaging.procedure;

import com.uco.ucopetapi.dto.procedure.ProcedureDTO;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProcedureDesactivationSenderBroker implements MessageSender<ProcedureDTO> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;

    @Value("${health.procedure.deactivate.exchange-name}")
    private String exchangeName;

    @Value("${health.procedure.deactivate.routing-key-name}")
    private String routingKey;

    public ProcedureDesactivationSenderBroker(final RabbitTemplate rabbitTemplate,
                                              final MapperJsonObject mapperJsonObject) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
    }

    @Override
    public void execute(final ProcedureDTO message, final String idMessage) {
        final String json = mapperJsonObject.execute(message)
                .orElseThrow(() -> new IllegalStateException(
                        "No fue posible serializar el mensaje del procedimiento"));
        rabbitTemplate.convertAndSend(exchangeName, routingKey, json, rabbitMessage -> {
            rabbitMessage.getMessageProperties().setMessageId(idMessage);
            rabbitMessage.getMessageProperties().setHeader("idMensaje", idMessage);
            return rabbitMessage;
        });
    }
}