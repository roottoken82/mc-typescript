package com.marco.mcts.commands;

import com.google.gson.JsonObject;
import com.marco.mcts.McTsMod;
import com.marco.mcts.ScriptManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Registers the /tsmacro command with sub-commands:
 *
 *   /tsmacro list               – list all known scripts and their status
 *   /tsmacro run <name>         – run a specific script
 *   /tsmacro stop <name>        – stop a specific running script
 *   /tsmacro reload <name>      – stop then restart a script (hot-reload)
 *   /tsmacro stopall            – stop all running scripts
 *   /tsmacro refresh            – refresh the script list from disk
 */
public class TsMacroCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("tsmacro")

                // /tsmacro list
                .then(Commands.literal("list")
                    .executes(ctx -> {
                        List<String> scriptNames = ScriptManager.getScripts();
                        if (scriptNames.isEmpty()) {
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§e[mc-typescript] Keine Skripte gefunden."), false);
                        } else {
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§e[mc-typescript] Skripte (" + scriptNames.size() + "):"), false);
                            for (String s : scriptNames) {
                                ScriptManager.ScriptStatus st = ScriptManager.getStatus(s);
                                String color = switch (st) {
                                    case RUNNING -> "§a";
                                    case ERROR   -> "§c";
                                    default      -> "§7";
                                };
                                String stLabel = switch (st) {
                                    case RUNNING -> "läuft";
                                    case ERROR   -> "Fehler";
                                    default      -> "idle";
                                };
                                final String line = "  " + color + s + " §8[" + stLabel + "]";
                                ctx.getSource().sendSuccess(() -> Component.literal(line), false);
                            }
                        }
                        return scriptNames.size();
                    })
                )

                // /tsmacro run <name>
                .then(Commands.literal("run")
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            if (!McTsMod.isModEnabled()) {
                                ctx.getSource().sendFailure(Component.literal(
                                    "§c[mc-typescript] Mod deaktiviert. Nutze /mcts enable."));
                                return 0;
                            }
                            String name = StringArgumentType.getString(ctx, "name");
                            if (McTsMod.getEventBroadcaster() != null) {
                                JsonObject d = new JsonObject();
                                d.addProperty("script", name);
                                McTsMod.getEventBroadcaster().broadcast("runScript", d);
                                ScriptManager.setStatus(name, ScriptManager.ScriptStatus.RUNNING, null);
                            }
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§a[mc-typescript] Starte: " + name), false);
                            return 1;
                        })
                    )
                )

                // /tsmacro stop <name>
                .then(Commands.literal("stop")
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            if (McTsMod.getEventBroadcaster() != null) {
                                McTsMod.getEventBroadcaster().broadcastStopScript(name);
                                ScriptManager.setStatus(name, ScriptManager.ScriptStatus.IDLE, null);
                            }
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§e[mc-typescript] Stoppe: " + name), false);
                            return 1;
                        })
                    )
                )

                // /tsmacro reload <name>
                .then(Commands.literal("reload")
                    .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            if (!McTsMod.isModEnabled()) {
                                ctx.getSource().sendFailure(Component.literal(
                                    "§c[mc-typescript] Mod deaktiviert. Nutze /mcts enable."));
                                return 0;
                            }
                            String name = StringArgumentType.getString(ctx, "name");
                            if (McTsMod.getEventBroadcaster() != null) {
                                McTsMod.getEventBroadcaster().broadcastReloadScript(name);
                                ScriptManager.setStatus(name, ScriptManager.ScriptStatus.RUNNING, null);
                            }
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("§a[mc-typescript] Lade neu: " + name), false);
                            return 1;
                        })
                    )
                )

                // /tsmacro stopall
                .then(Commands.literal("stopall")
                    .executes(ctx -> {
                        if (McTsMod.getEventBroadcaster() != null) {
                            McTsMod.getEventBroadcaster().broadcastStopAllScripts();
                            ScriptManager.resetAll();
                        }
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§e[mc-typescript] Alle Skripte gestoppt."), false);
                        return 1;
                    })
                )

                // /tsmacro refresh
                .then(Commands.literal("refresh")
                    .executes(ctx -> {
                        ScriptManager.refresh();
                        int count = ScriptManager.getScripts().size();
                        ctx.getSource().sendSuccess(
                            () -> Component.literal("§e[mc-typescript] Skriptliste aktualisiert – "
                                + count + " Skript(e) gefunden."), false);
                        return count;
                    })
                )
        );
    }
}
