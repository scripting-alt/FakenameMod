package com.felp.fakenamemod.events;

import com.felp.fakenamemod.client.FakeNameClientCache;
import com.felp.fakenamemod.data.FakeNameWorldData;
import com.felp.fakenamemod.network.FakeNamePayload;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerEvents {

    @SubscribeEvent(priority = EventPriority.LOW) // LOW = roda depois do VanishMod (NORMAL), então enxerga o [V] já setado
    public static void onNameFormat(PlayerEvent.NameFormat event) {
        String fakeName = null;

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            FakeNameWorldData data = FakeNameWorldData.get(serverPlayer.getServer().overworld());
            fakeName = data.getFakeName(serverPlayer.getUUID());
        } else if (event.getEntity().level().isClientSide()) {
            fakeName = FakeNameClientCache.getFakeName(event.getEntity().getUUID());
        }

        if (fakeName == null || fakeName.isEmpty()) return;

        Component currentDisplay = event.getDisplayname();

        if (currentDisplay != null && !currentDisplay.getSiblings().isEmpty()) {
            // Outro mod já modificou o displayName (ex: VanishMod colocou [V] na frente)
            // Preserva o prefixo e troca só o nome base (último filho)
            var siblings = currentDisplay.getSiblings();
            net.minecraft.network.chat.MutableComponent rebuilt = Component.literal("")
                    .withStyle(currentDisplay.getStyle());

            for (int i = 0; i < siblings.size(); i++) {
                if (i == siblings.size() - 1) {
                    // Último filho = nome do jogador → substitui pelo fakeName
                    rebuilt.append(Component.literal(fakeName)
                            .withStyle(siblings.get(i).getStyle()));
                } else {
                    rebuilt.append(siblings.get(i));
                }
            }
            event.setDisplayname(rebuilt);
        } else {
            // Nenhum outro mod mexeu ainda — seta o fakeName diretamente
            event.setDisplayname(Component.literal(fakeName));
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer joiningPlayer) {
            FakeNameWorldData data = FakeNameWorldData.get(joiningPlayer.getServer().overworld());

            String joiningFakeName = data.getFakeName(joiningPlayer.getUUID());
            if (joiningFakeName != null && !joiningFakeName.isEmpty()) {
                PacketDistributor.sendToAllPlayers(new FakeNamePayload(joiningPlayer.getUUID(), joiningFakeName));
                joiningPlayer.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(
                        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, joiningPlayer
                ));
            }

            for (ServerPlayer otherPlayer : joiningPlayer.getServer().getPlayerList().getPlayers()) {
                if (otherPlayer.getUUID().equals(joiningPlayer.getUUID())) continue;

                String otherFakeName = data.getFakeName(otherPlayer.getUUID());
                if (otherFakeName != null && !otherFakeName.isEmpty()) {
                    PacketDistributor.sendToPlayer(joiningPlayer, new FakeNamePayload(otherPlayer.getUUID(), otherFakeName));
                }
            }
        }
    }
}