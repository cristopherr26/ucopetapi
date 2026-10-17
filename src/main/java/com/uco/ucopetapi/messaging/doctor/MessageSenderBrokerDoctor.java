package com.uco.ucopetapi.messaging.doctor;

import com.uco.ucopetapi.domain.doctor.DoctorDomain;
import com.uco.ucopetapi.util.MessageSender;
import org.springframework.stereotype.Component;

@Component
public class MessageSenderBrokerDoctor implements MessageSender<DoctorDomain> {

    @Override
    public void execute(DoctorDomain message, String idMessage) {

    }
}
