package com.uco.ucopetapi.config.product;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:inventory.properties")
public class StockAdjustQueueConfig {

    @Value("${inventory.stock.adjust.exchange-name}")
    private String exchangeName;

    @Value("${inventory.stock.adjust.queue-name}")
    private String queueName;

    @Value("${inventory.stock.adjust.routing-key-name}")
    private String routingKey;

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
        return BindingBuilder.bind(stockAdjustQueue())
                .to(stockAdjustExchange())
                .with(routingKey);
    }
}
