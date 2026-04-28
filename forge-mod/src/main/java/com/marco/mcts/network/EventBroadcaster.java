package com.marco.mcts.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Builds and broadcasts event JSON payloads to all connected TypeScript clients.
 *
 * Protocol (event):
 * <pre>
 * { "event": "chat", "data": { "user": "Marco", "message": "hello" } }
 * </pre>
 */
public class EventBroadcaster {

    private static final Gson GSON = new Gson();

    private final WSServer wsServer;

    public EventBroadcaster(WSServer wsServer) {
        this.wsServer = wsServer;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Event helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Broadcast a chat message event. */
    public void broadcastChat(String user, String message) {
        JsonObject data = new JsonObject();
        data.addProperty("user", user);
        data.addProperty("message", message);
        broadcast("chat", data);
    }

    /** Broadcast a hotkey press event. */
    public void broadcastHotkey(String key) {
        JsonObject data = new JsonObject();
        data.addProperty("key", key);
        broadcast("hotkey", data);
    }

    /** Broadcast a game-tick event. */
    public void broadcastTick() {
        broadcast("tick", new JsonObject());
    }

    /** Broadcast a damage event. */
    public void broadcastDamage(String source, float amount) {
        JsonObject data = new JsonObject();
        data.addProperty("source", source);
        data.addProperty("amount", amount);
        broadcast("damage", data);
    }

    /** Broadcast a player-join event. */
    public void broadcastPlayerJoin(String name) {
        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        broadcast("playerJoin", data);
    }

    /** Broadcast a player-leave event. */
    public void broadcastPlayerLeave(String name) {
        JsonObject data = new JsonObject();
        data.addProperty("name", name);
        broadcast("playerLeave", data);
    }

    /** Broadcast a player-death event. */
    public void broadcastDeath() {
        broadcast("death", new JsonObject());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Generic broadcast
    // ──────────────────────────────────────────────────────────────────────────

    /** Broadcast a generic event with a data payload. */
    public void broadcast(String event, JsonObject data) {
        JsonObject msg = new JsonObject();
        msg.addProperty("event", event);
        msg.add("data", data);
        wsServer.broadcast(GSON.toJson(msg));
    }
}
