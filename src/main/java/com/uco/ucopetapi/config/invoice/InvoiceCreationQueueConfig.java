package com.uco.ucopetapi.config.invoice;

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
@PropertySource("classpath:payments.properties")
public class InvoiceCreationQueueConfig {

    @Value("${ucopet.payment.bindings.invoice-create.exchange}")
    private String exchangeName;

    @Value("${ucopet.payment.bindings.invoice-create.queue}")
    private String queueName;

    @Value("${ucopet.payment.bindings.invoice-create.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange invoiceCreationExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue invoiceCreationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding invoiceCreationBinding() {
        return BindingBuilder.bind(invoiceCreationQueue())
                .to(invoiceCreationExchange())
                .with(routingKey);
    }
}