package com.uco.ucopetapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "commercial.provider")
public record ProviderQueueProperties(Queue save,
                                      Queue deactivate) {
    public record Queue(String exchangeName, String queueName, String routingKeyName) {
    }
}
