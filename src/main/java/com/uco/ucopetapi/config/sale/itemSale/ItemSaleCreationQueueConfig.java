package com.uco.ucopetapi.config.sale.itemSale;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ItemSaleCreationQueueConfig {

    @Bean
    public DirectExchange itemSaleCreationExchange(
            @Value("${commercial.sale-item.save.exchange-name}")
            String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue itemSaleCreationQueue(
            @Value("${commercial.sale-item.save.queue-name}")
            String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding itemSaleCreationBinding(
            Queue itemSaleCreationQueue,
            DirectExchange itemSaleCreationExchange,
            @Value("${commercial.sale-item.save.routing-key-name}")
            String routingKey) {

        return BindingBuilder
                .bind(itemSaleCreationQueue)
                .to(itemSaleCreationExchange)
                .with(routingKey);
    }
}

