package com.uco.ucopetapi.messaging.headquarter;

import java.time.Instant;
import java.util.UUID;

public record DeactivateHeadquarterCommand(
        UUID headquarterId,
        Instant occurredAt
) implements HeadquarterCommand{}
