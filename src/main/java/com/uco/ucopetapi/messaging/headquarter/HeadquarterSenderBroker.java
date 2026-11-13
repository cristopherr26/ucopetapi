package com.uco.ucopetapi.messaging.headquarter;


import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import java.nio.charset.StandardCharsets;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class HeadquarterSenderBroker implements MessageSender<HeadquarterCommand> {

    private static final String PUBLISH_ERROR_MSG = "No se pudo registrar la operacion sobre la sede. Intente nuevamente.";
    private final RabbitTemplate rabbitTemplate;
    private final HeadquarterQueueConfig queueConfig;
    private final MapperJsonObject mapperJsonObject;

    public HeadquarterSenderBroker(RabbitTemplate rabbitTemplate,
                                   HeadquarterQueueConfig queueConfig,
                                   MapperJsonObject mapperJsonObject) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueConfig = queueConfig;
        this.mapperJsonObject = mapperJsonObject;
    }

    @Override
    public void execute(HeadquarterCommand command, String idMessage) {
        HeadquarterQueueConfig.Destiny destiny = resolveDestiny(command);
        String json = mapperJsonObject.execute(command)
                .orElseThrow(() -> new BusinessException(PUBLISH_ERROR_MSG));
        Message message = MessageBuilder.withBody(json.getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setMessageId(idMessage)
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();

        try {
            rabbitTemplate.send(destiny.getExchangeName(), destiny.getRoutingKeyName(), message);
        } catch (AmqpException e) {
            throw new BusinessException(PUBLISH_ERROR_MSG);
        }
    }

    private HeadquarterQueueConfig.Destiny resolveDestiny(HeadquarterCommand command) {
        return switch (command) {
            case SaveHeadquarterCommand ignored -> queueConfig.getSave();
            case DeactivateHeadquarterCommand ignored -> queueConfig.getDeactivate();
        };
    }
}
