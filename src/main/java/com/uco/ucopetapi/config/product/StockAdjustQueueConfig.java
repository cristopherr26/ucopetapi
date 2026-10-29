package com.uco.ucopetapi.config.product;

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
@ConfigurationProperties(prefix = "inventory.stock.adjust")
@PropertySource("classpath:inventory.properties")
public class StockAdjustQueueConfig {

    private String exchangeName;
    private String routingKeyName;
    private String queueName;

    @Bean
    public DirectExchange stockAdjustExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue stockAdjustQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding stockAdjustBinding() {
        return BindingBuilder.bind(stockAdjustQueue()).to(stockAdjustExchange()).with(routingKeyName);
    }

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
