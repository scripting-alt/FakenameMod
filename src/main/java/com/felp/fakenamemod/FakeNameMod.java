package com.felp.fakenamemod;

import com.felp.fakenamemod.client.FakeNameClientCache;
import com.felp.fakenamemod.command.FakeNameCommand;
import com.felp.fakenamemod.events.PlayerEvents;
import com.felp.fakenamemod.network.FakeNamePayload;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(FakeNameMod.MODID)
public class FakeNameMod {
    public static final String MODID = "fakenamemod";

    public FakeNameMod(IEventBus modEventBus) {
        // Registra o pacote de rede
        modEventBus.addListener(this::registerNetworking);
        
        // Registra eventos nativos (Comandos e NameFormat)
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        NeoForge.EVENT_BUS.register(PlayerEvents.class);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        FakeNameCommand.register(event.getDispatcher());
    }

    private void registerNetworking(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(
                FakeNamePayload.TYPE,
                FakeNamePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    // Atualiza o banco de dados temporário do cliente
                    FakeNameClientCache.setFakeName(payload.playerUUID(), payload.fakeName());
                    
                    // Atualiza a Nametag em tempo real no mundo do cliente
                    net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                    if (mc.level != null) {
                        net.minecraft.world.entity.player.Player clientPlayer = mc.level.getPlayerByUUID(payload.playerUUID());
                        if (clientPlayer != null) {
                            clientPlayer.refreshDisplayName();
                        }
                    }
                })
        );
    }
}