package com.uco.ucopetapi.config.order;

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
@PropertySource("classpath:health.properties")
public class OrderDesactivationQueueConfig {

    @Value("${health.order.deactivate.exchange-name}")
    private String exchangeName;

    @Value("${health.order.deactivate.queue-name}")
    private String queueName;

    @Value("${health.order.deactivate.routing-key-name}")
    private String routingKey;

    @Bean
    public DirectExchange orderDesactivationExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue orderDesactivationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding orderDesactivationBinding() {
        return BindingBuilder.bind(orderDesactivationQueue())
                .to(orderDesactivationExchange())
                .with(routingKey);
    }
}