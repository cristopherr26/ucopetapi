package com.uco.ucopetapi.messaging.sale;

import com.uco.ucopetapi.dto.sale.ItemSaleMessageDTO;
import com.uco.ucopetapi.util.gson.MapperJsonObject;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ItemSaleCreationSenderBroker
        extends SaleSenderBroker<ItemSaleMessageDTO> {

    public ItemSaleCreationSenderBroker(
            RabbitTemplate rabbitTemplate,
            MapperJsonObject mapperJsonObject,
            @Value("${commercial.sale-item.save.exchange-name}")
            String exchangeName,
            @Value("${commercial.sale-item.save.routing-key-name}")
            String routingKey) {

        super(
                rabbitTemplate,
                mapperJsonObject,
                exchangeName,
                routingKey
        );
    }
}