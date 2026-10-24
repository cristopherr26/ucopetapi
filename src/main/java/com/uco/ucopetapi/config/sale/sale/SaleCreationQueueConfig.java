package com.uco.ucopetapi.config.sale.sale;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:commercial.properties")

@Configuration
public class SaleCreationQueueConfig {

    @Value("${commercial.sale.save.exchange-name}")
    private String exchangeName;

    @Value("${commercial.sale.save.queue-name}")
    private String queueName;

    @Value("${commercial.sale.save.routing-key-name}")
    private String routingKey;

    @Bean
    public DirectExchange saleCreationExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue saleCreationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding saleCreationBinding() {
        return BindingBuilder.bind(saleCreationQueue())
                .to(saleCreationExchange())
                .with(routingKey);
    }
}