package com.uco.ucopetapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "inventory.procesar")
@PropertySource("classpath:inventory.properties")
public class InventoryQueueConfig {

    private boolean asyncWrites;

    public boolean isAsyncWrites() {
        return asyncWrites;
    }

    public void setAsyncWrites(final boolean asyncWrites) {
        this.asyncWrites = asyncWrites;
    }
}
