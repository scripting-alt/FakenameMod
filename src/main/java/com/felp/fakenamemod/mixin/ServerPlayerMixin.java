package com.felp.fakenamemod.mixin;

import com.felp.fakenamemod.data.FakeNameWorldData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayer.class, priority = 900) // prioridade menor = roda antes de outros mods (VanishMod usa 1000)
public abstract class ServerPlayerMixin {

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (player.getServer() == null || player.getServer().overworld() == null) return;

        FakeNameWorldData data = FakeNameWorldData.get(player.getServer().overworld());
        String fakeName = data.getFakeName(player.getUUID());

        if (fakeName == null || fakeName.isEmpty()) return;

        Component currentValue = cir.getReturnValue();

        if (currentValue != null) {
            // Outro mod (ex: VanishMod) já modificou o displayName — preserva e só troca o nome base
            // Reconstrói: pega o componente atual e substitui o texto folha pelo fakeName
            cir.setReturnValue(replaceLeafText(currentValue, fakeName));
        } else {
            cir.setReturnValue(Component.literal(fakeName));
        }
    }

    /**
     * Percorre o componente e substitui o último filho de texto puro pelo fakeName,
     * preservando prefixos como [V] que outros mods tenham adicionado.
     *
     * Estratégia: clona o componente raiz (que contém o prefixo [V]) e troca
     * apenas o texto do filho que representa o nome do jogador.
     */
    private static Component replaceLeafText(Component original, String fakeName) {
        // O VanishMod constrói: Component.literal("") + [V] + nome
        // O último filho é o nome — substituímos ele pelo fakeName.
        // Se o componente não tiver filhos (é texto puro), só substituímos o texto.
        var siblings = original.getSiblings();
        if (siblings.isEmpty()) {
            // Componente simples sem filhos — só substitui
            return Component.literal(fakeName).withStyle(original.getStyle());
        }

        // Tem filhos: copia a raiz com os irmãos, trocando o último pelo fakeName
        net.minecraft.network.chat.MutableComponent rebuilt = Component.literal("")
                .withStyle(original.getStyle());

        for (int i = 0; i < siblings.size(); i++) {
            if (i == siblings.size() - 1) {
                // Último filho = nome do jogador → substitui pelo fakeName
                rebuilt.append(Component.literal(fakeName)
                        .withStyle(siblings.get(i).getStyle()));
            } else {
                rebuilt.append(siblings.get(i));
            }
        }
        return rebuilt;
    }
}