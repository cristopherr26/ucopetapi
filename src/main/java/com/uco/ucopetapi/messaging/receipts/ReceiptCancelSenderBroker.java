package com.uco.ucopetapi.messaging.receipts;

import com.uco.ucopetapi.domain.receipts.ReceiptDomain;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ReceiptCancelSenderBroker extends ReceiptSenderBroker<ReceiptDomain> {

    public ReceiptCancelSenderBroker(
            final RabbitTemplate rabbitTemplate,
            final MapperJsonObject mapperJsonObject,
            @Value("${ucopet.payment.bindings.receipt-cancel.exchange}") final String exchangeName,
            @Value("${ucopet.payment.bindings.receipt-cancel.routing-key}") final String routingKey) {
        super(rabbitTemplate, mapperJsonObject, exchangeName, routingKey);
    }
}