package com.felp.fakenamemod.mixin;

import com.felp.fakenamemod.data.FakeNameWorldData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Shadow @Final private List<ServerPlayer> players;

    @Inject(method = "getPlayerByName", at = @At("HEAD"), cancellable = true)
    private void onGetPlayerByName(String name, CallbackInfoReturnable<ServerPlayer> cir) {
        if (name == null || name.isEmpty() || this.players.isEmpty()) return;

        net.minecraft.server.level.ServerLevel overworld = this.players.get(0).getServer().overworld();
        if (overworld == null) return;

        FakeNameWorldData data = FakeNameWorldData.get(overworld);

        for (ServerPlayer player : this.players) {
            String fakeName = data.getFakeName(player.getUUID());
            
            // Qual nome está valendo no momento?
            String activeName = (fakeName != null && !fakeName.isEmpty()) ? fakeName : player.getGameProfile().getName();

            if (activeName.equalsIgnoreCase(name)) {
                cir.setReturnValue(player);
                return;
            }
        }
        
        // Bloqueia a execução se usar o nick original enquanto estiver de fake
        cir.setReturnValue(null);
    }   
}