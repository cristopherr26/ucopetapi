package com.uco.ucopetapi.messaging.headquarter;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HeadquarterTopologyConfig {
    private final HeadquarterQueueConfig queueConfig;

    public HeadquarterTopologyConfig(HeadquarterQueueConfig queueConfig) {
        this.queueConfig = queueConfig;
    }

    @Bean
    public DirectExchange headquarterSaveExchange() {
        return ExchangeBuilder
                .directExchange(queueConfig.getSave().getExchangeName())
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange headquarterDeactivateExchange() {
        return ExchangeBuilder
                .directExchange(queueConfig.getDeactivate().getExchangeName())
                .durable(true)
                .build();
    }

    @Bean
    public MessageConverter jsonMessageCOnverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
