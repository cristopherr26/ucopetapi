package com.uco.ucopetapi.messaging.headquarter;

import java.time.Instant;
import java.util.UUID;

public record SaveHeadquarterCommand(
        UUID headquarterId,
        String name,
        String address,
        Boolean isActive,
        Instant occurredAt
) implements HeadquarterCommand {}
