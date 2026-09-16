package com.uco.ucopetapi.event;

import java.time.ZonedDateTime;
import java.util.UUID;

public class LoginSuccededEvent {

    private final UUID loginId;
    private final UUID personId;
    private final ZonedDateTime date;

    public LoginSuccededEvent (UUID loginId, UUID personId, ZonedDateTime date) {
        this.loginId = loginId;
        this. personId = personId;
        this.date = date;
    }

    public UUID getLoginId() { return loginId; }
    public UUID getPersonId() { return personId; }
    public ZonedDateTime getDate() { return date; }

}
