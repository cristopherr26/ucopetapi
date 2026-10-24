package com.uco.ucopetapi.config;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DisplayName("Colas del bus de inventario")
class InventoryQueueConfigTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private InventoryQueueConfig queueConfig;

    @Test
    @DisplayName("la API declara las cinco colas, cada una unida a su exchange con su routing key")
    void declaresTheFiveQueues() {
        Map<String, Binding> bindings = context.getBeansOfType(Binding.class).values().stream()
                .filter(binding -> binding.getDestination().startsWith("ucopet.inventory."))
                .collect(Collectors.toMap(Binding::getDestination, binding -> binding));

        assertThat(bindings).containsOnlyKeys(
                "ucopet.inventory.product.save.qu",
                "ucopet.inventory.product.deactivate.qu",
                "ucopet.inventory.service.save.qu",
                "ucopet.inventory.service.deactivate.qu",
                "ucopet.inventory.stock.adjust.qu");
        assertThat(bindings.get("ucopet.inventory.product.save.qu").getExchange())
                .isEqualTo("ucopet.inventory.product.save.ex");
        assertThat(bindings.get("ucopet.inventory.product.save.qu").getRoutingKey())
                .isEqualTo("ucopet.inventory.product.save.rk");
        assertThat(bindings.get("ucopet.inventory.stock.adjust.qu").getExchange())
                .isEqualTo("ucopet.inventory.stock.adjust.ex");
    }

    @Test
    @DisplayName("la escritura por el bus sigue apagada por defecto")
    void asyncWritesIsOffByDefault() {
        assertThat(queueConfig.isAsyncWrites()).isFalse();
    }
}
