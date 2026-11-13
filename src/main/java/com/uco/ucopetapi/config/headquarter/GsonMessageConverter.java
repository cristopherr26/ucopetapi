package com.uco.ucopetapi.config.headquarter;

import com.google.gson.Gson;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

public class GsonMessageConverter extends AbstractMessageConverter {
    private final Gson gson;

    public GsonMessageConverter(Gson gson) {
        this.gson = gson;
    }

    @Override
    protected Message createMessage(Object object, MessageProperties messageProperties) {
        byte[] body = gson.toJson(object).getBytes(StandardCharsets.UTF_8);

        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
        messageProperties.setContentLength(body.length);

        return new Message(body, messageProperties);
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        Type targetType = message.getMessageProperties().getInferredArgumentType();

        if(targetType == null) {
            throw new MessageConversionException(
                    "No se pudo determinar el tipo de destino del mensaje"
            );
        }

        String json = new String(message.getBody(), StandardCharsets.UTF_8);

        try {
            return gson.fromJson(json, targetType);
        } catch (Exception e) {
            throw new MessageConversionException("Invalid JSON to " + targetType, e);
        }
    }
}
