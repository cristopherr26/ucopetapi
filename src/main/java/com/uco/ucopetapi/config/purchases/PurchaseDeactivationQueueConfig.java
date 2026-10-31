package com.uco.ucopetapi.config.purchases;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "commercial.purchases.deactivate")
@PropertySource("classpath:commercial.properties")
public class PurchaseDeactivationQueueConfig {

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
