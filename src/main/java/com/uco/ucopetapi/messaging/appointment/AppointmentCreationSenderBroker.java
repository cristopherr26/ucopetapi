package com.uco.ucopetapi.messaging.appointment;

import com.uco.ucopetapi.dto.appointment.AppointmentDTO;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Publica en RabbitMQ la cita a crear o actualizar. El procesador de salud la guarda en la BD.
@Component
public class AppointmentCreationSenderBroker implements MessageSender<AppointmentDTO> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;

    @Value("${health.appointment.save.exchange-name}")
    private String exchangeName;

    @Value("${health.appointment.save.routing-key-name}")
    private String routingKey;

    public AppointmentCreationSenderBroker(final RabbitTemplate rabbitTemplate,
                                           final MapperJsonObject mapperJsonObject) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
    }

    @Override
    public void execute(final AppointmentDTO message, final String idMessage) {
        final String json = mapperJsonObject.execute(message)
                .orElseThrow(() -> new IllegalStateException(
                        "No fue posible serializar el mensaje de la cita"));
        rabbitTemplate.convertAndSend(exchangeName, routingKey, json, rabbitMessage -> {
            rabbitMessage.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
            rabbitMessage.getMessageProperties().setMessageId(idMessage);
            // El procesador lee este header para identificar el mensaje en sus logs
            rabbitMessage.getMessageProperties().setHeader("idMensaje", idMessage);
            return rabbitMessage;
        });
    }
}
