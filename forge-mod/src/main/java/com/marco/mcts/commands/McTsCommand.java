package com.marco.mcts.commands;

import com.marco.mcts.McTsMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/**
 * Registers the /mcts command with sub-commands:
 *   /mcts enable  – activate the mod (safety toggle)
 *   /mcts disable – deactivate the mod
 *   /mcts status  – show current status
 *   /mcts run <name>  – run a script
 *   /mcts stop    – stop all running scripts
 *   /mcts menu    – open the script selection GUI
 *   /mcts reload  – reload script list
 */
public class McTsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("mcts")
                // /mcts enable
                .then(Commands.literal("enable")
                    .executes(ctx -> {
                        McTsMod.setModEnabled(true);
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§a[mc-typescript] Mod ENABLED."), false);
                        return 1;
                    })
                )
                // /mcts disable
                .then(Commands.literal("disable")
                    .executes(ctx -> {
                        McTsMod.setModEnabled(false);
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§c[mc-typescript] Mod DISABLED."), false);
                        return 1;
                    })
                )
                // /mcts status
                .then(Commands.literal("status")
                    .executes(ctx -> {
                        boolean enabled = McTsMod.isModEnabled();
                        int clients = McTsMod.getWsServer() != null
                            ? McTsMod.getWsServer().getClientCount() : 0;
                        ctx.getSource().sendSuccess(
                            () -> Component.literal(String.format(
                                "§e[mc-typescript] Status: %s | WS clients: %d",
                                enabled ? "§aENABLED" : "§cDISABLED", clients
                            )), false);
                        return 1;
                    })
                )
                // /mcts run <name>
                .then(Commands.literal("run")
                    .then(Commands.argument("script", StringArgumentType.word())
                        .executes(ctx -> {
                            if (!McTsMod.isModEnabled()) {
                                ctx.getSource().sendFailure(Component.literal(
                                    "§c[mc-typescript] Mod is disabled. Use /mcts enable first."));
                                return 0;
                            }
                            String script = StringArgumentType.getString(ctx, "script");
                            // Notify the TS runtime via WS broadcast
                            if (McTsMod.getEventBroadcaster() != null) {
                                com.google.gson.JsonObject data = new com.google.gson.JsonObject();
                                data.addProperty("script", script);
                                McTsMod.getEventBroadcaster().broadcast("runScript", data);
                            }
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§e[mc-typescript] Running script: " + script), false);
                            return 1;
                        })
                    )
                )
                // /mcts stop
                .then(Commands.literal("stop")
                    .executes(ctx -> {
                        if (McTsMod.getEventBroadcaster() != null) {
                            McTsMod.getEventBroadcaster().broadcastStopAllScripts();
                            com.marco.mcts.ScriptManager.resetAll();
                        }
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§e[mc-typescript] Alle Skripte gestoppt."), false);
                        return 1;
                    })
                )
                // /mcts menu
                .then(Commands.literal("menu")
                    .executes(ctx -> {
                        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                        mc.execute(() -> mc.setScreen(
                            new com.marco.mcts.client.gui.TsMacroScreen(null)));
                        return 1;
                    })
                )
                // /mcts reload
                .then(Commands.literal("reload")
                    .executes(ctx -> {
                        if (McTsMod.getEventBroadcaster() != null) {
                            McTsMod.getEventBroadcaster().broadcast("reloadScripts",
                                new com.google.gson.JsonObject());
                        }
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§e[mc-typescript] Reload-Signal gesendet."), false);
                        return 1;
                    })
                )
        );
    }
}
