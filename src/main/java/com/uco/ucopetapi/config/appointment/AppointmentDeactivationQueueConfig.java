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

// Declara el exchange, la cola y el binding por donde viajan los cambios de estado de las citas
// (las citas no tienen "activo/inactivo": su equivalente es cancelar, confirmar o completar).
@Configuration
@PropertySource("classpath:health.properties")
public class AppointmentDeactivationQueueConfig {

    @Value("${health.appointment.deactivate.exchange-name}")
    private String exchangeName;

    @Value("${health.appointment.deactivate.queue-name}")
    private String queueName;

    @Value("${health.appointment.deactivate.routing-key-name}")
    private String routingKey;

    @Bean
    public DirectExchange appointmentDeactivationExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue appointmentDeactivationQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding appointmentDeactivationBinding() {
        return BindingBuilder.bind(appointmentDeactivationQueue())
                .to(appointmentDeactivationExchange())
                .with(routingKey);
    }
}
