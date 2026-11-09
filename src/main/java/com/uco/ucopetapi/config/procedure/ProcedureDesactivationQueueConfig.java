package com.uco.ucopetapi.config.procedure;

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
@PropertySource("classpath:health.properties")
public class ProcedureDesactivationQueueConfig {

    @Value("${health.procedure.deactivate.exchange-name}")
    private String exchangeName;

    @Value("${health.procedure.deactivate.queue-name}")
    private String queueName;

    @Value("${health.procedure.deactivate.routing-key-name}")
    private String routingKey;

    @Bean
    public DirectExchange procedureDesactivationExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue procedureDesactivationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding procedureDesactivationBinding() {
        return BindingBuilder.bind(procedureDesactivationQueue())
                .to(procedureDesactivationExchange())
                .with(routingKey);
    }
}