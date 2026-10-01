package com.uco.ucopetapi.crosscutting.helpers;

import java.util.UUID;

public final class UUIDHelper
{
    private static final UUIDHelper INSTANCE = new UUIDHelper();
    private static final String UUID_DEFAULT_AS_STRING = "00000000-0000-0000-0000-000000000000";

    private UUIDHelper()
    {

    }

    public static UUIDHelper getUUIDHelper()
    {
        return INSTANCE;
    }

    public UUID getDefault()
    {
        return getFromString(UUID_DEFAULT_AS_STRING);
    }

    public UUID getDefault(final UUID value)
    {
        return ObjectHelper.getDefault(value, getDefault());
    }

    public UUID getFromString(final String uuidAsString)
    {
        if(uuidAsString == null || " ".equals(uuidAsString))
        {
            return getDefault();
        }

        else
        {
            return UUID.fromString(uuidAsString);
        }
    }

}
