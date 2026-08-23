package com.felp.fakenamemod.mixin.client;

import com.mojang.authlib.GameProfile;
import com.felp.fakenamemod.client.FakeNameClientCache;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/**
 * Este mixin intercepta o método getProfile() do PlayerInfo
 * para retornar um GameProfile com o nome falso quando necessário.
 * 
 * Isso garante que mods como o Figura que acessam diretamente
 * GameProfile.getName() vejam o nome falso.
 */
@Mixin(PlayerInfo.class)
public class PlayerInfoMixin {
    
    @Inject(method = "getProfile", at = @At("RETURN"), cancellable = true)
    private void onGetProfile(CallbackInfoReturnable<GameProfile> cir) {
        GameProfile originalProfile = cir.getReturnValue();
        UUID playerUUID = originalProfile.getId();
        
        // Obtém o nome falso do cache do cliente
        String fakeName = FakeNameClientCache.getFakeName(playerUUID);
        
        // Se houver um nome falso, cria um novo GameProfile com o nome falso
        if (fakeName != null && !fakeName.isEmpty()) {
            GameProfile fakeProfile = new GameProfile(playerUUID, fakeName);
            
            // Copia as propriedades (texturas, capas, etc.) do perfil original
            fakeProfile.getProperties().putAll(originalProfile.getProperties());
            
            // Retorna o perfil modificado
            cir.setReturnValue(fakeProfile);
        }
    }
}