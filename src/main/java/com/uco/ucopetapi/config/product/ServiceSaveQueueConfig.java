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
public class ServiceSaveQueueConfig {

    @Value("${inventory.service.save.exchange-name}")
    private String exchangeName;

    @Value("${inventory.service.save.queue-name}")
    private String queueName;

    @Value("${inventory.service.save.routing-key-name}")
    private String routingKey;

    @Bean
    public DirectExchange serviceSaveExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue serviceSaveQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding serviceSaveBinding() {
        return BindingBuilder.bind(serviceSaveQueue())
                .to(serviceSaveExchange())
                .with(routingKey);
    }
}
