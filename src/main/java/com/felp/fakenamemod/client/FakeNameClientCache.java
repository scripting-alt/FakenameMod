package com.felp.fakenamemod.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakeNameClientCache {
    private static final Map<UUID, String> FAKE_NAMES = new HashMap<>();

    public static void setFakeName(UUID uuid, String name) {
        if (name == null || name.isEmpty()) {
            FAKE_NAMES.remove(uuid);
        } else {
            FAKE_NAMES.put(uuid, name);
        }
    }

    public static String getFakeName(UUID uuid) {
        return FAKE_NAMES.get(uuid);
    }
}