package com.felp.fakenamemod.mixin.client;

import com.felp.fakenamemod.client.FakeNameClientCache;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {

    // Agora usamos Inject no HEAD para assumir o controle total da criação do nome na TabList
    @Inject(method = "getNameForDisplay", at = @At("HEAD"), cancellable = true)
    private void onGetNameForDisplay(PlayerInfo playerInfo, CallbackInfoReturnable<Component> cir) {
        if (playerInfo == null || playerInfo.getProfile() == null) return;

        UUID uuid = playerInfo.getProfile().getId();
        String fakeName = FakeNameClientCache.getFakeName(uuid);

        if (fakeName != null && !fakeName.isEmpty()) {
            // 1. Cria o texto base apenas com o Nick Falso
            MutableComponent fakeNameComponent = Component.literal(fakeName);

            // 2. Puxa o time original do jogador
            PlayerTeam team = playerInfo.getTeam();
            
            // 3. Se ele estiver em um time, envelopa o nick com o Gradiente (Prefix), Cor e Suffix!
            if (team != null) {
                fakeNameComponent = team.getFormattedName(fakeNameComponent);
            }

            // 4. Retorna para a tela o componente completinho e cancela o método original
            cir.setReturnValue(fakeNameComponent);
        }
    }
}