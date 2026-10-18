package com.uco.ucopetapi.messages.provider;

import com.uco.ucopetapi.config.ProviderQueueProperties;
import com.uco.ucopetapi.dto.provider.ProviderDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProviderSavePublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ProviderQueueProperties properties;
    private final MapperJsonObject mapper;
    public ProviderSavePublisher(RabbitTemplate rabbitTemplate,
                                 ProviderQueueProperties properties,
                                 MapperJsonObject mapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
        this.mapper = mapper;
    }
    public void publish(ProviderDTO provider) {
        String json = mapper.execute(provider)
                .orElseThrow(() -> new IllegalStateException("No se pudo preparar el mensaje del proveedor"));
        rabbitTemplate.convertAndSend(
                properties.save().exchangeName(),
                properties.save().routingKeyName(),
                json);
    }
}
