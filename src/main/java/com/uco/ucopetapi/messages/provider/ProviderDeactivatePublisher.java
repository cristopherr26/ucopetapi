package com.uco.ucopetapi.messages.provider;

import com.uco.ucopetapi.config.provider.ProviderDeactivateQueueConfig;
import com.uco.ucopetapi.dto.provider.ProviderDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProviderDeactivatePublisher {

    private final RabbitTemplate rabbitTemplate;

    private final ProviderDeactivateQueueConfig config;

    private final MapperJsonObject mapper;
    public ProviderDeactivatePublisher(RabbitTemplate rabbitTemplate,
                                       ProviderDeactivateQueueConfig config,
                                       MapperJsonObject mapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.config = config;
        this.mapper = mapper;
    }
    public void publish(ProviderDTO provider) {
        String json = mapper.execute(provider)
                .orElseThrow(() -> new IllegalStateException("No se pudo preparar el mensaje del proveedor"));
        rabbitTemplate.convertAndSend(
                config.getExchangeName(),
                config.getRoutingKeyName(),
                json);
    }
}
