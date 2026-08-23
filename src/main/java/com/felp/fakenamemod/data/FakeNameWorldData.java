package com.felp.fakenamemod.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakeNameWorldData extends SavedData {
    private final Map<UUID, String> fakeNames = new HashMap<>();

    public static FakeNameWorldData get(ServerLevel level) {
        ServerLevel overworld = level.getServer().getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld == null) overworld = level;
        
        return overworld.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(FakeNameWorldData::new, FakeNameWorldData::load, null),
                "fakenamemod_data"
        );
    }

    public String getFakeName(UUID uuid) {
        return fakeNames.get(uuid);
    }

    public void setFakeName(UUID uuid, String name) {
        if (name == null || name.isEmpty()) {
            fakeNames.remove(uuid);
        } else {
            fakeNames.put(uuid, name);
        }
        setDirty();
    }

    public static FakeNameWorldData load(CompoundTag tag, HolderLookup.Provider provider) {
        FakeNameWorldData data = new FakeNameWorldData();
        CompoundTag playersTag = tag.getCompound("Players");
        for (String key : playersTag.getAllKeys()) {
            data.fakeNames.put(UUID.fromString(key), playersTag.getString(key));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, String> entry : fakeNames.entrySet()) {
            playersTag.putString(entry.getKey().toString(), entry.getValue());
        }
        tag.put("Players", playersTag);
        return tag;
    }
}