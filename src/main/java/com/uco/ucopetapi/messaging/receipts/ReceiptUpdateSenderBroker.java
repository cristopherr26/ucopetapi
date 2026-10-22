package com.uco.ucopetapi.messaging.receipts;

import com.uco.ucopetapi.dto.receipts.ReceiptMessageDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReceiptUpdateSenderBroker extends ReceiptSenderBroker<ReceiptMessageDTO> {

    public ReceiptUpdateSenderBroker(
            final RabbitTemplate rabbitTemplate,
            final MapperJsonObject mapperJsonObject,
            @Value("${ucopet.payment.bindings.receipt-update.exchange}") final String exchangeName,
            @Value("${ucopet.payment.bindings.receipt-update.routing-key}") final String routingKey) {
        super(rabbitTemplate, mapperJsonObject, exchangeName, routingKey);
    }
}
