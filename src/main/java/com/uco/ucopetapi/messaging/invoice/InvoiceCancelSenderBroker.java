package com.uco.ucopetapi.messaging.invoice;

import com.uco.ucopetapi.dto.invoice.InvoiceCancelMessageDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InvoiceCancelSenderBroker extends InvoiceSenderBroker<InvoiceCancelMessageDTO> {

    public InvoiceCancelSenderBroker(
            final RabbitTemplate rabbitTemplate,
            final MapperJsonObject mapperJsonObject,
            @Value("${ucopet.payment.bindings.invoice-cancel.exchange}") final String exchangeName,
            @Value("${ucopet.payment.bindings.invoice-cancel.routing-key}") final String routingKey) {
        super(rabbitTemplate, mapperJsonObject, exchangeName, routingKey);
    }
}