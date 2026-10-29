package com.uco.ucopetapi.config.sale;

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
public class SaleUpdateQueueConfig {

    @Value("${commercial.sale.update.exchange-name}")
    private String exchangeName;

    @Value("${commercial.sale.update.queue-name}")
    private String queueName;

    @Value("${commercial.sale.update.routing-key-name}")
    private String routingKey;

    @Bean
    public DirectExchange saleUpdateExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue saleUpdateQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding saleUpdateBinding() {
        return BindingBuilder.bind(saleUpdateQueue())
                .to(saleUpdateExchange())
                .with(routingKey);
    }
}
