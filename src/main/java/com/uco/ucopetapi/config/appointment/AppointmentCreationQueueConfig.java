package com.uco.ucopetapi.config.appointment;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

// Declara el exchange, la cola y el binding por donde viajan las citas a crear o actualizar.
// Los nombres salen de health.properties; el procesador de salud escucha esta misma cola.
@Configuration
@PropertySource("classpath:health.properties")
public class AppointmentCreationQueueConfig {

    @Value("${health.appointment.save.exchange-name}")
    private String exchangeName;

    @Value("${health.appointment.save.queue-name}")
    private String queueName;

    @Value("${health.appointment.save.routing-key-name}")
    private String routingKey;

    @Bean
    public DirectExchange appointmentCreationExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue appointmentCreationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding appointmentCreationBinding() {
        return BindingBuilder.bind(appointmentCreationQueue())
                .to(appointmentCreationExchange())
                .with(routingKey);
    }
}
