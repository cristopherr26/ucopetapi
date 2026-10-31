package com.uco.ucopetapi.messaging.sale.sale;

import com.uco.ucopetapi.dto.sale.sale.SaleDeactivateMessageDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SaleDeactivateSenderBroker
        extends SaleSenderBroker<SaleDeactivateMessageDTO> {

    public SaleDeactivateSenderBroker(
            RabbitTemplate rabbitTemplate,
            MapperJsonObject mapperJsonObject,
            @Value("${commercial.sale.deactivate.exchange-name}")
            String exchangeName,
            @Value("${commercial.sale.deactivate.routing-key-name}")
            String routingKey) {

        super(
                rabbitTemplate,
                mapperJsonObject,
                exchangeName,
                routingKey
        );
    }
}
