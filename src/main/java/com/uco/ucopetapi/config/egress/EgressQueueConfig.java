package com.uco.ucopetapi.config.egress;

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
public class EgressQueueConfig {

    @Value("${payments.egress.save.exchange-name}")
    private String saveExchangeName;
    @Value("${payments.egress.save.queue-name}")
    private String saveQueueName;
    @Value("${payments.egress.save.routing-key-name}")
    private String saveRoutingKey;

    @Value("${payments.egress.delete.exchange-name}")
    private String deleteExchangeName;
    @Value("${payments.egress.delete.queue-name}")
    private String deleteQueueName;
    @Value("${payments.egress.delete.routing-key-name}")
    private String deleteRoutingKey;

    // ---------- Crear / actualizar ----------
    @Bean
    public DirectExchange egressSaveExchange() {
        return new DirectExchange(saveExchangeName, true, false);
    }

    @Bean
    public Queue egressSaveQueue() {
        return QueueBuilder.durable(saveQueueName).build();
    }

    @Bean
    public Binding egressSaveBinding() {
        return BindingBuilder.bind(egressSaveQueue())
                .to(egressSaveExchange())
                .with(saveRoutingKey);
    }

    // ---------- Eliminar ----------
    @Bean
    public DirectExchange egressDeleteExchange() {
        return new DirectExchange(deleteExchangeName, true, false);
    }

    @Bean
    public Queue egressDeleteQueue() {
        return QueueBuilder.durable(deleteQueueName).build();
    }

    @Bean
    public Binding egressDeleteBinding() {
        return BindingBuilder.bind(egressDeleteQueue())
                .to(egressDeleteExchange())
                .with(deleteRoutingKey);
    }
}