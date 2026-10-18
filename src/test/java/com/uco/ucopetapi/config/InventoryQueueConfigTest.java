package com.uco.ucopetapi.config;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("Propiedades del bus de inventario")
class InventoryQueueConfigTest {

    @Autowired
    private InventoryQueueProperties queues;

    @Autowired
    private InventoryQueueConfig queueConfig;

    @Test
    @DisplayName("inventory.properties carga las cinco colas con el patron save y deactivate")
    void loadsTheFiveQueues() {
        assertThat(queues.product().save().exchangeName()).isEqualTo("ucopet.inventory.product.save.ex");
        assertThat(queues.product().save().queueName()).isEqualTo("ucopet.inventory.product.save.qu");
        assertThat(queues.product().save().routingKeyName()).isEqualTo("ucopet.inventory.product.save.rk");
        assertThat(queues.product().deactivate().queueName()).isEqualTo("ucopet.inventory.product.deactivate.qu");
        assertThat(queues.service().save().queueName()).isEqualTo("ucopet.inventory.service.save.qu");
        assertThat(queues.service().deactivate().queueName()).isEqualTo("ucopet.inventory.service.deactivate.qu");
        assertThat(queues.stock().adjust().queueName()).isEqualTo("ucopet.inventory.stock.adjust.qu");
    }

    @Test
    @DisplayName("crear y editar van por save, dar de baja por deactivate, y el stock por adjust")
    void mapsTheSevenCommandsToTheirQueue() {
        assertThat(queues.forCommand("product-create")).isEqualTo(queues.product().save());
        assertThat(queues.forCommand("product-update")).isEqualTo(queues.product().save());
        assertThat(queues.forCommand("product-deactivate")).isEqualTo(queues.product().deactivate());
        assertThat(queues.forCommand("service-create")).isEqualTo(queues.service().save());
        assertThat(queues.forCommand("service-update")).isEqualTo(queues.service().save());
        assertThat(queues.forCommand("service-deactivate")).isEqualTo(queues.service().deactivate());
        assertThat(queues.forCommand("stock-adjust")).isEqualTo(queues.stock().adjust());
    }

    @Test
    @DisplayName("un comando sin cola falla diciendo cual")
    void unknownCommandFailsLoudly() {
        assertThatThrownBy(() -> queues.forCommand("product-inventado"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("product-inventado");
    }

    @Test
    @DisplayName("la escritura por el bus sigue apagada por defecto")
    void asyncWritesIsOffByDefault() {
        assertThat(queueConfig.isAsyncWrites()).isFalse();
    }
}
