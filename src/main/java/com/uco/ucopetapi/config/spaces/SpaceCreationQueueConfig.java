package com.uco.ucopetapi.config.spaces;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

//Nombres de exchange, routing key y cola de Space para: crear y actualizar.
 //Se leen de health.properties (prefijo health.space.save)
@Configuration
@ConfigurationProperties(prefix = "health.space.save")
public class SpaceCreationQueueConfig {

    private String exchangeName;
    private String routingKeyName;
    private String queueName;

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {

        this.exchangeName = exchangeName;
    }

    public String getRoutingKeyName() {

        return routingKeyName;
    }

    public void setRoutingKeyName(String routingKeyName) {

        this.routingKeyName = routingKeyName;
    }

    public String getQueueName() {

        return queueName;
    }

    public void setQueueName(String queueName) {

        this.queueName = queueName;
    }
}

