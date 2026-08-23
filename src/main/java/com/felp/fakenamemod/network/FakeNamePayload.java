package com.felp.fakenamemod.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record FakeNamePayload(UUID playerUUID, String fakeName) implements CustomPacketPayload {

    public static final Type<FakeNamePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("fakenamemod", "sync_fakename"));

    public static final StreamCodec<FriendlyByteBuf, FakeNamePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, FakeNamePayload::playerUUID,
            ByteBufCodecs.STRING_UTF8, FakeNamePayload::fakeName,
            FakeNamePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}