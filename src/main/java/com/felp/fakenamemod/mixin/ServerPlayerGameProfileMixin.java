package com.felp.fakenamemod.mixin;

import com.felp.fakenamemod.FakeNameHelper;
import com.felp.fakenamemod.data.FakeNameWorldData;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class ServerPlayerGameProfileMixin {

    @Inject(method = "getGameProfile", at = @At("RETURN"), cancellable = true)
    private void injectGetGameProfile(CallbackInfoReturnable<GameProfile> cir) {
        Object self = this;
        if (!(self instanceof ServerPlayer player)) return;

        if (FakeNameHelper.isBypassed()) return;

        if (player.getServer() == null || player.getServer().overworld() == null) return;

        FakeNameWorldData data = FakeNameWorldData.get(player.getServer().overworld());
        String fakeName = data.getFakeName(player.getUUID());

        if (fakeName != null && !fakeName.isEmpty()) {
            GameProfile original = cir.getReturnValue();
            GameProfile fakeProfile = new GameProfile(original.getId(), fakeName);
            fakeProfile.getProperties().putAll(original.getProperties());
            cir.setReturnValue(fakeProfile);
        }
    }
}