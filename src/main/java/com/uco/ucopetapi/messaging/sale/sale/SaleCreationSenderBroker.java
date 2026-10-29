package com.uco.ucopetapi.messaging.sale.sale;

import com.uco.ucopetapi.dto.sale.sale.SaleOrderMessageDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SaleCreationSenderBroker
        extends SaleSenderBroker<SaleOrderMessageDTO> {

    public SaleCreationSenderBroker(
            RabbitTemplate rabbitTemplate,
            MapperJsonObject mapperJsonObject,
            @Value("${commercial.sale.save.exchange-name}")
            String exchangeName,
            @Value("${commercial.sale.save.routing-key-name}")
            String routingKey) {

        super(
                rabbitTemplate,
                mapperJsonObject,
                exchangeName,
                routingKey
        );
    }
}
