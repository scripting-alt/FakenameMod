package com.felp.fakenamemod.mixin.client;

import com.felp.fakenamemod.client.FakeNameClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mixin(ClientSuggestionProvider.class)
public abstract class ClientSuggestionProviderMixin {

    // Agora sim, injetando na classe correta que gera as sugestões do TAB!
    @Inject(method = "getOnlinePlayerNames", at = @At("HEAD"), cancellable = true)
    private void onGetOnlinePlayerNames(CallbackInfoReturnable<Collection<String>> cir) {
        List<String> names = new ArrayList<>();
        
        try {
            // Pega a conexão atual do jogo de forma super segura
            ClientPacketListener connection = Minecraft.getInstance().getConnection();
            
            if (connection != null) {
                Collection<PlayerInfo> players = connection.getOnlinePlayers();
                
                if (players != null) {
                    for (PlayerInfo playerInfo : players) {
                        // Ignora jogadores fantasmas carregando
                        if (playerInfo == null || playerInfo.getProfile() == null) continue;

                        UUID uuid = playerInfo.getProfile().getId();
                        if (uuid == null) continue;

                        // Consulta o seu cache
                        String fakeName = FakeNameClientCache.getFakeName(uuid);

                        // Substitui a sugestão
                        if (fakeName != null && !fakeName.isEmpty()) {
                            names.add(fakeName);
                        } else {
                            String realName = playerInfo.getProfile().getName();
                            if (realName != null) {
                                names.add(realName);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Previne qualquer crash louco durante o login
        }
        
        // Retorna nossa lista modificada para a caixinha do chat
        cir.setReturnValue(names);
    }
}