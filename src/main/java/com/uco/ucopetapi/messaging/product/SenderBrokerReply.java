package com.uco.ucopetapi.messaging.product;

import com.uco.ucopetapi.dto.product.ProcessorReplyDTO;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.UUID;

final class SenderBrokerReply {

    private static final long EXPIRATION_MS = 4000;

    private SenderBrokerReply() {
    }

    static void setDeadline(MessageProperties properties) {
        properties.setExpiration(String.valueOf(EXPIRATION_MS));
        properties.setHeader("plazo", System.currentTimeMillis() + EXPIRATION_MS);
    }

    static ProcessorReplyDTO toReply(Message reply) {
        MessageProperties properties = reply.getMessageProperties();
        Object result = properties.getHeader("resultado");
        Object type = properties.getHeader("tipo");
        Object reason = properties.getHeader("motivo");
        Object stockId = properties.getHeader("stockId");
        Object quantity = properties.getHeader("cantidad");
        return new ProcessorReplyDTO("aplicado".equals(result), "nombre-repetido".equals(type),
                reason != null ? reason.toString() : null,
                stockId != null ? UUID.fromString(stockId.toString()) : null,
                quantity instanceof Number number ? number.intValue() : null);
    }
}
