package com.uco.ucopetapi.messaging.headquarter;


import com.uco.ucopetapi.exception.BusinessException;
import com.uco.ucopetapi.util.MessageSender;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class HeadquarterSenderBroker implements MessageSender<HeadquarterCommand> {

    private static final String PUBLISH_ERROR_MSG = "No se pudo registrar la operacion sobre la sede. Intente nuevamente.";
    private final RabbitTemplate rabbitTemplate;
    private final HeadquarterQueueConfig queueConfig;

    public HeadquarterSenderBroker(RabbitTemplate rabbitTemplate,
                                   HeadquarterQueueConfig queueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.queueConfig = queueConfig;
    }

    @Override
    public void execute(HeadquarterCommand command, String idMessage) {
        HeadquarterQueueConfig.Destiny destiny = resolveDestiny(command);

        try {
            rabbitTemplate.convertAndSend(
                    destiny.getExchangeName(),
                    destiny.getRoutingKeyName(),
                    command,
                    message -> {
                        message.getMessageProperties().setMessageId(idMessage);
                        message.getMessageProperties()
                                .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    }
            );
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
