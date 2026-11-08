package com.uco.ucopetapi.messaging.invoice;

import com.uco.ucopetapi.dto.invoice.InvoiceCreateMessageDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InvoiceCreationSenderBroker extends InvoiceSenderBroker<InvoiceCreateMessageDTO> {

    public InvoiceCreationSenderBroker(
            final RabbitTemplate rabbitTemplate,
            final MapperJsonObject mapperJsonObject,
            @Value("${ucopet.payment.bindings.invoice-create.exchange}") final String exchangeName,
            @Value("${ucopet.payment.bindings.invoice-create.routing-key}") final String routingKey) {
        super(rabbitTemplate, mapperJsonObject, exchangeName, routingKey);
    }
}