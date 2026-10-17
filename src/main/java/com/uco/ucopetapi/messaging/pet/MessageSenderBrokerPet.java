package com.uco.ucopetapi.messaging.pet;

import com.uco.ucopetapi.domain.pet.PetDomain;
import com.uco.ucopetapi.util.MessageSender;
import org.springframework.stereotype.Component;

@Component
public class MessageSenderBrokerPet implements MessageSender<PetDomain> {

    @Override
    public void execute(PetDomain message, String idMessage) {

    }
}
