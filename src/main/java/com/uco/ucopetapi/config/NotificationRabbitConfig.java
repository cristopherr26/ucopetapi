package com.uco.ucopetapi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationRabbitConfig {

    @Value("${notification.create.exchange}") private String createEx;
    @Value("${notification.create.queue}")    private String createQu;
    @Value("${notification.create.routingkey}") private String createRk;

    @Value("${notification.delete.exchange}") private String deleteEx;
    @Value("${notification.delete.queue}")    private String deleteQu;
    @Value("${notification.delete.routingkey}") private String deleteRk;

    @Bean
    DirectExchange notificationCreateExchange() { return new DirectExchange(createEx, true, false); }
    @Bean
    Queue notificationCreateQueue()             { return new Queue(createQu, true); }
    @Bean
    Binding notificationCreateBinding() {
        return BindingBuilder.bind(notificationCreateQueue())
                .to(notificationCreateExchange()).with(createRk);
    }

    @Bean DirectExchange notificationDeleteExchange() { return new DirectExchange(deleteEx, true, false); }
    @Bean Queue notificationDeleteQueue()             { return new Queue(deleteQu, true); }
    @Bean Binding notificationDeleteBinding() {
        return BindingBuilder.bind(notificationDeleteQueue())
                .to(notificationDeleteExchange()).with(deleteRk);
    }
}