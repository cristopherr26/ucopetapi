package com.uco.ucopetapi.messaging.appointment;

import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Publica en RabbitMQ el cambio de estado de una cita (id, estado y justificacion de cancelacion).
@Component
public class AppointmentDeactivationSenderBroker implements MessageSender<AppointmentDTO> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;

    @Value("${health.appointment.deactivate.exchange-name}")
    private String exchangeName;

    @Value("${health.appointment.deactivate.routing-key-name}")
    private String routingKey;

    public AppointmentDeactivationSenderBroker(final RabbitTemplate rabbitTemplate,
                                               final MapperJsonObject mapperJsonObject) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
    }

    @Override
    public void execute(final AppointmentDTO message, final String idMessage) {
        final String json = mapperJsonObject.execute(message)
                .orElseThrow(() -> new IllegalStateException(
                        "No fue posible serializar el mensaje de cambio de estado de la cita"));
        rabbitTemplate.convertAndSend(exchangeName, routingKey, json, rabbitMessage -> {
            rabbitMessage.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
            rabbitMessage.getMessageProperties().setMessageId(idMessage);
            // El procesador lee este header para identificar el mensaje en sus logs
            rabbitMessage.getMessageProperties().setHeader("idMensaje", idMessage);
            return rabbitMessage;
        });
    }
}
