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
public class ReceiptUpdateQueueConfig {

    @Value("${ucopet.payment.bindings.receipt-update.exchange}")
    private String exchangeName;

    @Value("${ucopet.payment.bindings.receipt-update.queue}")
    private String queueName;

    @Value("${ucopet.payment.bindings.receipt-update.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange receiptUpdateExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue receiptUpdateQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding receiptUpdateBinding() {
        return BindingBuilder.bind(receiptUpdateQueue())
                .to(receiptUpdateExchange())
                .with(routingKey);
    }
}
