package com.marco.mcts.actions;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Handles chat-related actions: sending messages and commands.
 */
public class ChatActions {

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /**
     * Send a chat message as the player.
     * Params: { "message": "Hello!" }
     */
    public JsonObject sendChat(JsonObject params) {
        String message = params.get("message").getAsString();
        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        // Truncate to Minecraft's 256-char limit
        if (message.length() > 256) {
            message = message.substring(0, 256);
        }
        player.chat(message);
        return new JsonObject();
    }

    /**
     * Execute a command without the leading slash.
     * Params: { "cmd": "gamemode creative" }
     */
    public JsonObject sendCommand(JsonObject params) {
        String cmd = params.get("cmd").getAsString();
        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        player.connection.sendCommand(cmd);
        return new JsonObject();
    }
}
