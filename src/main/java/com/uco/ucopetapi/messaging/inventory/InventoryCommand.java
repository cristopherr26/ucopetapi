package com.uco.ucopetapi.messaging.inventory;

import java.util.Map;

public record InventoryCommand(String command, Map<String, Object> data) {
}
