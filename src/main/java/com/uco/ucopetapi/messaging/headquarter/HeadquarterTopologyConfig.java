package com.uco.ucopetapi.messaging.headquarter;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
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
}
