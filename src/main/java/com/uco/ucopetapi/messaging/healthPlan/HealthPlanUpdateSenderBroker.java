package com.uco.ucopetapi.messaging.healthPlan;

import com.uco.ucopetapi.config.healthPlan.HealthPlanUpdateQueueConfig;
import com.uco.ucopetapi.domain.healthPlan.HealthPlanDomain;
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
public class HealthPlanUpdateSenderBroker implements MessageSender<HealthPlanDomain> {

    private final RabbitTemplate rabbitTemplate;
    private final MapperJsonObject mapperJsonObject;
    private final HealthPlanUpdateQueueConfig healthPlanUpdateQueueConfig;

    public HealthPlanUpdateSenderBroker(RabbitTemplate rabbitTemplate,
                                        MapperJsonObject mapperJsonObject,
                                        HealthPlanUpdateQueueConfig healthPlanUpdateQueueConfig) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapperJsonObject = mapperJsonObject;
        this.healthPlanUpdateQueueConfig = healthPlanUpdateQueueConfig;
    }

    @Override
    public void execute(HealthPlanDomain message, String idMessage) {
        MessageProperties messageProperties = generateMessageProperties(idMessage);

        Map<String, Object> payload = new HashMap<>();
        payload.put("id", message.getId());
        payload.put("name", message.getName());
        payload.put("description", message.getDescription());
        payload.put("insuranceCompany", message.getInsuranceCompany());
        payload.put("status", message.getStatus());
        payload.put("deleted", message.isDeleted());

        Optional<Message> messageBody = getMessageBody(payload, messageProperties);
        if (!messageBody.isPresent()) {
            return;
        }

        rabbitTemplate.convertAndSend(
                healthPlanUpdateQueueConfig.getExchangeName(),
                healthPlanUpdateQueueConfig.getRoutingKeyName(),
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