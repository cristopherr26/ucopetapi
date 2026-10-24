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
public class ItemSaleUpdateQueueConfig {

    @Bean
    public DirectExchange itemSaleUpdateExchange(
            @Value("${commercial.sale-item.update.exchange-name}")
            String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue itemSaleUpdateQueue(
            @Value("${commercial.sale-item.update.queue-name}")
            String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding itemSaleUpdateBinding(
            Queue itemSaleUpdateQueue,
            DirectExchange itemSaleUpdateExchange,
            @Value("${commercial.sale-item.update.routing-key-name}")
            String routingKey) {

        return BindingBuilder
                .bind(itemSaleUpdateQueue)
                .to(itemSaleUpdateExchange)
                .with(routingKey);
    }
}
