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

/**
 * Exchange, cola y binding para registrar un pago de factura (mismo patron que ReceiptCreationQueueConfig).
 */
@Configuration
@PropertySource("classpath:payments.properties")
public class InvoicePaymentQueueConfig {

    @Value("${ucopet.payment.bindings.invoice-payment.exchange}")
    private String exchangeName;

    @Value("${ucopet.payment.bindings.invoice-payment.queue}")
    private String queueName;

    @Value("${ucopet.payment.bindings.invoice-payment.routing-key}")
    private String routingKey;

    @Bean
    public DirectExchange invoicePaymentExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue invoicePaymentQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding invoicePaymentBinding() {
        return BindingBuilder.bind(invoicePaymentQueue())
                .to(invoicePaymentExchange())
                .with(routingKey);
    }
}