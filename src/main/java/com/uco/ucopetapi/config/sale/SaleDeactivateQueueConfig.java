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
public class SaleDeactivateQueueConfig {

    @Value("${commercial.sale.deactivate.exchange-name}")
    private String exchangeName;

    @Value("${commercial.sale.deactivate.queue-name}")
    private String queueName;

    @Value("${commercial.sale.deactivate.routing-key-name}")
    private String routingKey;

    @Bean
    public DirectExchange saleDeactivateExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue saleDeactivateQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding saleDeactivateBinding() {
        return BindingBuilder.bind(saleDeactivateQueue())
                .to(saleDeactivateExchange())
                .with(routingKey);
    }
}
