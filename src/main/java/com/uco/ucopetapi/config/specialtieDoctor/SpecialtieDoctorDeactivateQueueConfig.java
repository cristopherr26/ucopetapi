package com.uco.ucopetapi.config.specialtieDoctor;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "user.specialtie-doctor.deactivate")
@PropertySource("classpath:user.properties")
public class SpecialtieDoctorDeactivateQueueConfig {

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