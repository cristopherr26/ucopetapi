package com.uco.ucopetapi.messaging.headquarter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uco.ucopetapi.exception.BusinessException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class HeadquarterSenderBroker implements HeadquarterCommandSender {

    private static final String PUBLISH_ERROR_MSG =
            "No se pudo registrar la operación sobre la sede. Intente nuevamente.";

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantTypeAdapter().nullSafe())
            .create();

    private final RabbitTemplate rabbitTemplate;
    private final HeadquarterQueueConfig queueConfig;

    public HeadquarterSenderBroker(RabbitTemplate rabbitTemplate,
                                   HeadquarterQueueConfig queueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueConfig = queueConfig;
    }

    @Override
    public void execute(HeadquarterCommand command, String idMessage) {
        HeadquarterQueueConfig.Destino destino = resolverDestino(command);
        String json = GSON.toJson(command);

        try {
            rabbitTemplate.convertAndSend(
                    destino.getExchangeName(),
                    destino.getRoutingKeyName(),
                    json,
                    mensaje -> {
                        mensaje.getMessageProperties().setMessageId(idMessage);
                        mensaje.getMessageProperties()
                               .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return mensaje;
                    });
        } catch (AmqpException e) {
            throw new BusinessException(PUBLISH_ERROR_MSG);
        }
    }

    private HeadquarterQueueConfig.Destino resolverDestino(HeadquarterCommand command) {
        return switch (command) {
            case SaveHeadquarterCommand ignored -> queueConfig.getSave();
            case DeactivateHeadquarterCommand ignored -> queueConfig.getDeactivate();
        };
    }
}
