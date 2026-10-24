package com.uco.ucopetapi.config.receipts;

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
public class ReceiptCreationQueueConfig {

    @Value("${ucopet.payment.bindings.receipt-create.exchange}")
    private String exchangeName;

    @Value("${ucopet.payment.bindings.receipt-create.queue}")
    private String queueName;

    @Value("${ucopet.payment.bindings.receipt-create.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange receiptCreationExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue receiptCreationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding receiptCreationBinding() {
        return BindingBuilder.bind(receiptCreationQueue())
                .to(receiptCreationExchange())
                .with(routingKey);
    }
}
