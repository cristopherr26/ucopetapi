package com.uco.ucopetapi.messaging.sale.sale;

import com.uco.ucopetapi.dto.sale.sale.SaleOrderMessageDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SaleUpdateSenderBroker
        extends SaleSenderBroker<SaleOrderMessageDTO> {

    public SaleUpdateSenderBroker(
            RabbitTemplate rabbitTemplate,
            MapperJsonObject mapperJsonObject,
            @Value("${commercial.sale.update.exchange-name}")
            String exchangeName,
            @Value("${commercial.sale.update.routing-key-name}")
            String routingKey) {

        super(
                rabbitTemplate,
                mapperJsonObject,
                exchangeName,
                routingKey
        );
    }
}
