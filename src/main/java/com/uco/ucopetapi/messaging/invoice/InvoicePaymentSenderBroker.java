package com.uco.ucopetapi.messaging.invoice;

import com.uco.ucopetapi.dto.invoice.InvoicePaymentMessageDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InvoicePaymentSenderBroker extends InvoiceSenderBroker<InvoicePaymentMessageDTO> {

    public InvoicePaymentSenderBroker(
            final RabbitTemplate rabbitTemplate,
            final MapperJsonObject mapperJsonObject,
            @Value("${ucopet.payment.bindings.invoice-payment.exchange}") final String exchangeName,
            @Value("${ucopet.payment.bindings.invoice-payment.routing-key}") final String routingKey) {
        super(rabbitTemplate, mapperJsonObject, exchangeName, routingKey);
    }
}