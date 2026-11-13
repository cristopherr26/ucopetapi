package com.uco.ucopetapi.config.transfer;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "inventory.transfer.save")
@PropertySource("classpath:inventory.properties")
public class TransferSaveQueueConfig {

    private String exchangeName;
    private String routingKeyName;
    private String queueName;

    @Bean
    public DirectExchange transferSaveExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue transferSaveQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding transferSaveBinding() {
        return BindingBuilder.bind(transferSaveQueue()).to(transferSaveExchange()).with(routingKeyName);
    }

    public String getExchangeName() { return exchangeName; }
    public void setExchangeName(String exchangeName) { this.exchangeName = exchangeName; }
    public String getRoutingKeyName() { return routingKeyName; }
    public void setRoutingKeyName(String routingKeyName) { this.routingKeyName = routingKeyName; }
    public String getQueueName() { return queueName; }
    public void setQueueName(String queueName) { this.queueName = queueName; }
}