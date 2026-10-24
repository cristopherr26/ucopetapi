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
public class ServiceDeactivationQueueConfig {

    @Value("${inventory.service.deactivate.exchange-name}")
    private String exchangeName;

    @Value("${inventory.service.deactivate.queue-name}")
    private String queueName;

    @Value("${inventory.service.deactivate.routing-key-name}")
    private String routingKey;

    @Bean
    public DirectExchange serviceDeactivationExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue serviceDeactivationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding serviceDeactivationBinding() {
        return BindingBuilder.bind(serviceDeactivationQueue())
                .to(serviceDeactivationExchange())
                .with(routingKey);
    }
}
