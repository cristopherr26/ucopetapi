package com.uco.ucopetapi.config;

import java.util.NoSuchElementException;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inventory")
public record InventoryQueueProperties(
        ModuleQueues product,
        ModuleQueues service,
        StockQueues stock) {

    public record ModuleQueues(Queue save, Queue deactivate) {
    }

    public record StockQueues(Queue adjust) {
    }

    public record Queue(String exchangeName, String queueName, String routingKeyName) {
    }

    public Queue forCommand(String command) {
        return switch (command) {
            case "product-create", "product-update" -> product.save();
            case "product-deactivate" -> product.deactivate();
            case "service-create", "service-update" -> service.save();
            case "service-deactivate" -> service.deactivate();
            case "stock-adjust" -> stock.adjust();
            default -> throw new NoSuchElementException("No hay cola configurada para el comando " + command);
        };
    }
}
