package com.felp.fakenamemod;

public class FakeNameHelper {
    private static final ThreadLocal<Boolean> BYPASS = ThreadLocal.withInitial(() -> false);

    public static void setBypass(boolean value) {
        BYPASS.set(value);
    }

    public static boolean isBypassed() {
        return BYPASS.get();
    }
}   