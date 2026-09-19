package com.uco.ucopetapi.crosscutting.helpers;

public final class IntHelper {
    public static int DEFAULT = 0;
    public static int MIN_VALUE = 1;
    private IntHelper() {
    }
    public static int getDefault() {
        return DEFAULT;
    }
    public static int getDefault(final int value) {
        return ObjectHelper.getDefault(value, DEFAULT);
    }
    public static int getValidStopAddress(final int value) {
        int safedValue = getDefault(value);
        return safedValue < MIN_VALUE ? DEFAULT : safedValue;
    }

}
