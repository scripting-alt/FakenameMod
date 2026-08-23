package com.felp.fakenamemod.command;

import com.felp.fakenamemod.data.FakeNameWorldData;
import com.felp.fakenamemod.network.FakeNamePayload;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class FakeNameCommand {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("FakeNameMod");
    
    // §f§l = branco negrito | §r§7 = cinza normal | §a = verde
    private static final String PREFIX = "§f§lFAKENAME §r§7>> §a";
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fakename")
            .requires(source -> source.hasPermission(2))
            
            .then(Commands.literal("set")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("newname", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                            String newName = StringArgumentType.getString(context, "newname");
                            CommandSourceStack source = context.getSource();
                            UUID targetUUID = target.getUUID();
                            
                            FakeNameWorldData data = FakeNameWorldData.get(target.getServer().overworld());
                            
                            // ✅ PEGA O NOME FAKE ATUAL (ANTES DE MUDAR)
                            String previousName = data.getFakeName(targetUUID);
                            if (previousName == null || previousName.isEmpty()) {
                                previousName = target.getGameProfile().getName();
                            }
                            
                            data.setFakeName(targetUUID, newName);
                            
                            PacketDistributor.sendToAllPlayers(new FakeNamePayload(targetUUID, newName));
                            target.refreshDisplayName();
                            
                            target.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(
                                    ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, target
                            ));
                            
                            boolean isSelf = source.getEntity() instanceof ServerPlayer sp &&
                                    sp.getUUID().equals(targetUUID);
                            
                            boolean logEnabled = target.serverLevel().getGameRules()
                                    .getBoolean(GameRules.RULE_LOGADMINCOMMANDS);
                            
                            // Sempre envia para quem executou
                            String selfMsg = isSelf
                                    ? "You changed your name to " + newName
                                    : "You changed " + previousName + " name to " + newName;
                            source.sendSuccess(() -> Component.literal(PREFIX + selfMsg), false);
                            
                            // Só loga e notifica OPs se logAdminCommands = true
                            if (logEnabled) {
                                String plainMsg = isSelf
                                        ? "changed their name to " + newName
                                        : "changed " + previousName + " name to " + newName;
                                String consoleLog = "[" + source.getTextName() + ": FAKENAME >> " + plainMsg + "]";
                                LOGGER.info(consoleLog);
                                
                                for (ServerPlayer online : source.getServer().getPlayerList().getPlayers()) {
                                    if (source.getServer().getPlayerList().isOp(online.getGameProfile())) {
                                        if (online.getUUID().equals(source.getEntity() != null
                                                ? source.getEntity().getUUID() : null)) continue;
                                        
                                        online.sendSystemMessage(Component.literal(
                                            "§7§o[" + source.getTextName() + ": FAKENAME >> " + plainMsg + "]"
                                        ));
                                    }
                                }
                            }
                            
                            return 1;
                        })
                    )
                )
            )
            
            .then(Commands.literal("clear")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> {
                        ServerPlayer target = EntityArgument.getPlayer(context, "target");
                        CommandSourceStack source = context.getSource();
                        UUID targetUUID = target.getUUID();
                        
                        FakeNameWorldData data = FakeNameWorldData.get(target.getServer().overworld());
                        
                        // ✅ PEGA O NOME FAKE ATUAL (ANTES DE LIMPAR)
                        String previousName = data.getFakeName(targetUUID);
                        String realName = target.getGameProfile().getName();
                        
                        // Se não tiver fake name, avisa e não faz nada
                        if (previousName == null || previousName.isEmpty()) {
                            source.sendSuccess(() -> Component.literal(PREFIX + realName + " doesn't have a fake name"), false);
                            return 0;
                        }
                        
                        data.setFakeName(targetUUID, "");
                        
                        PacketDistributor.sendToAllPlayers(new FakeNamePayload(targetUUID, ""));
                        target.refreshDisplayName();
                        
                        target.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(
                            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, target
                        ));
                        
                        boolean isSelf = source.getEntity() instanceof ServerPlayer sp &&
                                sp.getUUID().equals(targetUUID);
                        
                        boolean logEnabled = target.serverLevel().getGameRules()
                                .getBoolean(GameRules.RULE_LOGADMINCOMMANDS);
                        
                        // Sempre envia para quem executou
                        String selfMsg = isSelf
                                ? "You reset your name to " + realName
                                : "You reset " + previousName + " name to " + realName;
                        source.sendSuccess(() -> Component.literal(PREFIX + selfMsg), false);
                        
                        // Só loga e notifica OPs se logAdminCommands = true
                        if (logEnabled) {
                            String plainMsg = isSelf
                                    ? "reset their name to " + realName
                                    : "reset " + previousName + " name to " + realName;
                            String consoleLog = "[" + source.getTextName() + ": FAKENAME >> " + plainMsg + "]";
                            LOGGER.info(consoleLog);
                            
                            for (ServerPlayer online : source.getServer().getPlayerList().getPlayers()) {
                                if (source.getServer().getPlayerList().isOp(online.getGameProfile())) {
                                    if (online.getUUID().equals(source.getEntity() != null
                                            ? source.getEntity().getUUID() : null)) continue;
                                    
                                    online.sendSystemMessage(Component.literal(
                                        "§7§o[" + source.getTextName() + ": FAKENAME >> " + plainMsg + "]"
                                    ));
                                }
                            }
                        }
                        
                        return 1;
                    })
                )
            )
        );
    }
}