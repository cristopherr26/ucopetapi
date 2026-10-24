package com.uco.ucopetapi.messaging.headquarter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:inventory.properties")
@ConfigurationProperties(prefix = "inventory.headquarter")
public class HeadquarterQueueConfig {
    private Destiny save = new Destiny();
    private Destiny deactivate = new Destiny();

    public static class Destiny {
        private String exchangeName;
        private String queueName;
        private String routingKeyName;

        public String getExchangeName() { return exchangeName; }
        public void setExchangeName(String exchangeName) { this.exchangeName = exchangeName; }
        public String getQueueName() { return queueName; }
        public void setQueueName(String queueName) { this.queueName = queueName; }
        public String getRoutingKeyName() { return this.routingKeyName; }
        public void setRoutingKeyName(String routingKeyName) { this.routingKeyName = routingKeyName; }
    }

    public Destiny getSave() {
        return save;
    }

    private void setSave(Destiny save) {
        this.save = save;
    }

    public Destiny getDeactivate() {
        return deactivate;
    }

    private void setDeactivate(Destiny deactivate) {
        this.deactivate = deactivate;
    }
}
