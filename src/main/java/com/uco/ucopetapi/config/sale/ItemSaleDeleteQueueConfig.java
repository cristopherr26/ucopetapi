package com.uco.ucopetapi.config.sale;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ItemSaleDeleteQueueConfig {

    @Bean
    public DirectExchange itemSaleDeleteExchange(
            @Value("${commercial.sale-item.delete.exchange-name}")
            String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue itemSaleDeleteQueue(
            @Value("${commercial.sale-item.delete.queue-name}")
            String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding itemSaleDeleteBinding(
            Queue itemSaleDeleteQueue,
            DirectExchange itemSaleDeleteExchange,
            @Value("${commercial.sale-item.delete.routing-key-name}")
            String routingKey) {

        return BindingBuilder
                .bind(itemSaleDeleteQueue)
                .to(itemSaleDeleteExchange)
                .with(routingKey);
    }
}

