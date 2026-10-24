package com.uco.ucopetapi.messaging.sale;

import com.uco.ucopetapi.dto.sale.ItemSaleDeleteMessageDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ItemSaleDeleteSenderBroker
        extends SaleSenderBroker<ItemSaleDeleteMessageDTO> {

    public ItemSaleDeleteSenderBroker(
            RabbitTemplate rabbitTemplate,
            MapperJsonObject mapperJsonObject,
            @Value("${commercial.sale-item.delete.exchange-name}")
            String exchangeName,
            @Value("${commercial.sale-item.delete.routing-key-name}")
            String routingKey) {

        super(
                rabbitTemplate,
                mapperJsonObject,
                exchangeName,
                routingKey
        );
    }
}
