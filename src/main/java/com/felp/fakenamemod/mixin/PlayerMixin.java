package com.felp.fakenamemod.mixin;

import com.felp.fakenamemod.FakeNameHelper;
import com.felp.fakenamemod.data.FakeNameWorldData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    private String getFakeNameForPlayer(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            FakeNameWorldData data = FakeNameWorldData.get(serverLevel);
            return data.getFakeName(player.getUUID());
        }
        return null;
    }

    @Inject(method = "getName", at = @At("RETURN"), cancellable = true)
    private void injectGetName(CallbackInfoReturnable<Component> cir) {
        // ⭐ Usa FakeNameHelper
        if (FakeNameHelper.isBypassed()) return;

        Player player = (Player)(Object)this;
        String fakeName = getFakeNameForPlayer(player);
        
        if (fakeName != null && !fakeName.isEmpty()) {
            cir.setReturnValue(Component.literal(fakeName));
        }
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void injectGetDisplayName(CallbackInfoReturnable<Component> cir) {
        if (FakeNameHelper.isBypassed()) return;

        Player player = (Player)(Object)this;
        String fakeName = getFakeNameForPlayer(player);
        
        if (fakeName != null && !fakeName.isEmpty()) {
            Component fakeComponent = Component.literal(fakeName);
            if (player.getTeam() != null) {
                cir.setReturnValue(player.getTeam().getFormattedName(fakeComponent));
            } else {
                cir.setReturnValue(fakeComponent);
            }
        }
    }

    @Inject(method = "getScoreboardName", at = @At("RETURN"), cancellable = true)
    private void injectGetScoreboardName(CallbackInfoReturnable<String> cir) {
        if (FakeNameHelper.isBypassed()) return;

        Player player = (Player)(Object)this;
        String fakeName = getFakeNameForPlayer(player);
        
        if (fakeName != null && !fakeName.isEmpty()) {
            cir.setReturnValue(fakeName);
        }
    }
}