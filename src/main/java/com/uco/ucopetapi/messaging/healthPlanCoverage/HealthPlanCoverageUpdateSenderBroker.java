package com.uco.ucopetapi.messaging.healthPlanCoverage;

import com.uco.ucopetapi.config.healthPlanCoverage.HealthPlanCoverageUpdateQueueConfig;
import com.uco.ucopetapi.domain.healthPlanCoverage.HealthPlanCoverageDomain;
import com.uco.ucopetapi.util.MessageSender;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class HealthPlanCoverageUpdateSenderBroker implements MessageSender<HealthPlanCoverageDomain> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final HealthPlanCoverageUpdateQueueConfig healthPlanCoverageUpdateQueueConfig;

    public HealthPlanCoverageUpdateSenderBroker(RabbitTemplate rabbitTemplate,
                                                MapperJsonObject mapperJsonObject,
                                                HealthPlanCoverageUpdateQueueConfig healthPlanCoverageUpdateQueueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.healthPlanCoverageUpdateQueueConfig = healthPlanCoverageUpdateQueueConfig;
    }

    @Override
    public void execute(HealthPlanCoverageDomain message, String idMessage) {
        MessageProperties messageProperties = generateMessageProperties(idMessage);

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", message.getId());
        payload.put("healthPlanId", message.getHealthPlan() != null ? message.getHealthPlan().getId() : null);
        payload.put("serviceId", message.getServiceId());
        payload.put("procedureId", message.getProcedureId()); // <-- ¡Agregado aquí también!
        payload.put("coveragePercentage", message.getCoveragePercentage());
        payload.put("coverageLimit", message.getCoverageLimit());
        payload.put("deleted", message.isDeleted());

        Optional<Message> messageBody = getMessageBody(payload, messageProperties);
        if (!messageBody.isPresent()) {
            return;
        }

        rabbitTemplate.convertAndSend(
                healthPlanCoverageUpdateQueueConfig.getExchangeName(),
                healthPlanCoverageUpdateQueueConfig.getRoutingKeyName(),
                messageBody.get()
        );
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
                .withBody(msg.getBytes())
                .andProperties(messageProperties)
                .build());
    }
}