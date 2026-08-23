package com.felp.fakenamemod.mixin;

import com.felp.fakenamemod.data.FakeNameWorldData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin {

    @Shadow public abstract MinecraftServer getServer();

    // Intercepta a lista de sugestões (Autocomplete) e mostra só os Fakes
    @Inject(method = "getOnlinePlayerNames", at = @At("HEAD"), cancellable = true)
    private void onGetOnlinePlayerNames(CallbackInfoReturnable<Collection<String>> cir) {
        MinecraftServer server = this.getServer();
        if (server == null) return;
        
        List<String> names = new ArrayList<>();
        FakeNameWorldData data = FakeNameWorldData.get(server.overworld());
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String fakeName = data.getFakeName(player.getUUID());
            if (fakeName != null && !fakeName.isEmpty()) {
                names.add(fakeName); // Substitui no autocomplete pelo nome falso
            } else {
                names.add(player.getGameProfile().getName()); // Se não tiver, mantém o original
            }
        }
        
        cir.setReturnValue(names);
    }
}