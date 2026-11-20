package com.uco.ucopetapi.config.specialtieDoctor;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

// Las claves de user.properties están en camelCase (user.specialtieDoctor.create.*);
// el binder de @ConfigurationProperties las resuelve contra este prefijo en kebab-case.
@Configuration
@ConfigurationProperties(prefix = "user.specialtie-doctor.create")
@PropertySource("classpath:user.properties")
public class SpecialtieDoctorCreateQueueConfig {

    private String exchangeName;
    private String routingKeyName;
    private String queueName;

    @Bean
    public DirectExchange specialtieDoctorCreateExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Queue specialtieDoctorCreateQueue() {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding specialtieDoctorCreateBinding() {
        return BindingBuilder.bind(specialtieDoctorCreateQueue())
                .to(specialtieDoctorCreateExchange())
                .with(routingKeyName);
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKeyName() {
        return routingKeyName;
    }

    public void setRoutingKeyName(String routingKeyName) {
        this.routingKeyName = routingKeyName;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
