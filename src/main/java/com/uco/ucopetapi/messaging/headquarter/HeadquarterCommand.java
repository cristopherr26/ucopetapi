package com.uco.ucopetapi.messaging.headquarter;

import java.time.Instant;
import java.util.UUID;

public sealed interface HeadquarterCommand
        permits SaveHeadquarterCommand, DeactivateHeadquarterCommand{
    UUID headquarterId();
}
