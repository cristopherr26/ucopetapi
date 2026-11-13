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
public class ReceiptCancelQueueConfig {

    @Value("${ucopet.payment.bindings.receipt-cancel.exchange}")
    private String exchangeName;

    @Value("${ucopet.payment.bindings.receipt-cancel.queue}")
    private String queueName;

    @Value("${ucopet.payment.bindings.receipt-cancel.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange receiptCancelExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue receiptCancelQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding receiptCancelBinding() {
        return BindingBuilder.bind(receiptCancelQueue())
                .to(receiptCancelExchange())
                .with(routingKey);
    }
}
