package com.uco.ucopetapi.messaging.receipts;

import com.uco.ucopetapi.domain.receipts.ReceiptDomain;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReceiptCreationSenderBroker extends ReceiptSenderBroker<ReceiptDomain> {

    public ReceiptCreationSenderBroker(
            final RabbitTemplate rabbitTemplate,
            final MapperJsonObject mapperJsonObject,
            @Value("${ucopet.payment.bindings.receipt-create.exchange}") final String exchangeName,
            @Value("${ucopet.payment.bindings.receipt-create.routing-key}") final String routingKey) {
        super(rabbitTemplate, mapperJsonObject, exchangeName, routingKey);
    }
}
