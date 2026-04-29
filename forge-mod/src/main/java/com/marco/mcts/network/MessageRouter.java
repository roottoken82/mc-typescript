package com.marco.mcts.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marco.mcts.McTsMod;
import com.marco.mcts.ScriptManager;
import com.marco.mcts.actions.*;
import net.minecraft.client.Minecraft;
import org.java_websocket.WebSocket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Routes incoming JSON requests from the TypeScript client to the correct action handler.
 *
 * Protocol (request):
 * <pre>
 * { "id": "abc", "action": "chat.send", "params": { "message": "hello" } }
 * </pre>
 *
 * Protocol (response):
 * <pre>
 * { "id": "abc", "ok": true, "result": { ... } }
 * { "id": "abc", "ok": false, "error": "Something went wrong" }
 * </pre>
 */
public class MessageRouter {

    private static final Logger LOGGER = LogManager.getLogger("mcts.MessageRouter");
    private static final Gson GSON = new Gson();

    private final WSServer wsServer;

    // Action handlers (one per action group)
    private final ChatActions chatActions = new ChatActions();
    private final MovementActions movementActions = new MovementActions();
    private final InteractionActions interactionActions = new InteractionActions();
    private final InventoryActions inventoryActions = new InventoryActions();
    private final ContainerActions containerActions = new ContainerActions();
    private final WorldActions worldActions = new WorldActions();

    public MessageRouter(WSServer wsServer) {
        this.wsServer = wsServer;
        wsServer.setRouter(this);
    }

    /** Route an incoming JSON message from a WebSocket client. */
    public void route(WebSocket conn, String json) {
        String requestId = null;
        try {
            JsonObject msg = JsonParser.parseString(json).getAsJsonObject();
            requestId = msg.has("id") ? msg.get("id").getAsString() : null;
            String action = msg.has("action") ? msg.get("action").getAsString() : null;
            JsonObject params = msg.has("params") ? msg.get("params").getAsJsonObject() : new JsonObject();

            if (action == null) {
                sendError(conn, requestId, "Missing 'action' field");
                return;
            }

            // Safety toggle: block all actions unless the mod is enabled
            if (!McTsMod.isModEnabled() && !action.startsWith("scriptHost.")) {
                sendError(conn, requestId, "Mod is disabled. Use /mcts enable in-game.");
                return;
            }

            LOGGER.debug("[MessageRouter] Action: {} | Params: {}", action, params);

            final String finalRequestId = requestId;
            final JsonObject finalParams = params;

            // All MC operations must run on the main thread
            Minecraft.getInstance().execute(() -> {
                try {
                    JsonObject result = dispatch(action, finalParams, conn);
                    sendOk(conn, finalRequestId, result);
                } catch (Exception e) {
                    LOGGER.error("[MessageRouter] Error executing action '{}': {}", action, e.getMessage());
                    sendError(conn, finalRequestId, e.getMessage());
                }
            });

        } catch (Exception e) {
            LOGGER.error("[MessageRouter] Failed to parse message: {}", json);
            sendError(conn, requestId, "Parse error: " + e.getMessage());
        }
    }

    /** Dispatch to the appropriate action handler. */
    private JsonObject dispatch(String action, JsonObject params, WebSocket conn) {
        return switch (action) {
            // ── Player ──────────────────────────────────────────────────────
            case "player.pos"       -> WorldActions.playerPos();
            case "player.health"    -> WorldActions.playerHealth();
            case "player.hunger"    -> WorldActions.playerHunger();
            case "player.yaw"       -> WorldActions.playerYaw();
            case "player.pitch"     -> WorldActions.playerPitch();
            case "player.status"    -> WorldActions.playerStatus();
            case "player.gameMode"  -> WorldActions.playerGameMode();
            case "player.dimension" -> WorldActions.playerDimension();

            // ── Chat ────────────────────────────────────────────────────────
            case "chat.send"    -> chatActions.sendChat(params);
            case "chat.command" -> chatActions.sendCommand(params);

            // ── Movement ────────────────────────────────────────────────────
            case "move.forward"  -> movementActions.forward(params);
            case "move.back"     -> movementActions.back(params);
            case "move.left"     -> movementActions.left(params);
            case "move.right"    -> movementActions.right(params);
            case "move.jump"     -> movementActions.jump();
            case "move.sneak"    -> movementActions.sneak(params);
            case "move.sprint"   -> movementActions.sprint(params);
            case "move.lookAt"   -> movementActions.lookAt(params);
            case "move.setLook"  -> movementActions.setLook(params);
            case "move.walkTo"   -> movementActions.walkTo(params);
            case "move.stop"     -> movementActions.stop();

            // ── Interaction ─────────────────────────────────────────────────
            case "interact.attack"          -> interactionActions.attack();
            case "interact.use"             -> interactionActions.use();
            case "interact.startBreaking"   -> interactionActions.startBreaking();
            case "interact.stopBreaking"    -> interactionActions.stopBreaking();
            case "interact.placeBlockAt"    -> interactionActions.placeBlockAt(params);
            case "interact.useItemOnEntity" -> interactionActions.useItemOnEntity(params);
            case "interact.attackEntity"    -> interactionActions.attackEntity(params);

            // ── Inventory ───────────────────────────────────────────────────
            case "inv.list"          -> inventoryActions.list();
            case "inv.selectHotbar"  -> inventoryActions.selectHotbar(params);
            case "inv.swap"          -> inventoryActions.swap(params);
            case "inv.drop"          -> inventoryActions.drop(params);
            case "inv.findItem"      -> inventoryActions.findItem(params);

            // ── Container ───────────────────────────────────────────────────
            case "chest.openAt"    -> containerActions.openAt(params);
            case "chest.slots"     -> containerActions.slots();
            case "chest.take"      -> containerActions.take(params);
            case "chest.put"       -> containerActions.put(params);
            case "chest.quickMove" -> containerActions.quickMove(params);
            case "chest.close"     -> containerActions.close();

            // ── World ───────────────────────────────────────────────────────
            case "world.getBlock"      -> worldActions.getBlock(params);
            case "world.raycast"       -> worldActions.raycast(params);
            case "world.entitiesNear"  -> worldActions.entitiesNear(params);
            case "world.findBlock"     -> worldActions.findBlock(params);
            case "world.spawnPoint"    -> worldActions.spawnPoint();

            // ── ScriptHost ──────────────────────────────────────────────────
            case "scriptHost.listScripts"  -> scriptHostListScripts();
            case "scriptHost.scriptStatus" -> scriptHostScriptStatus(params);

            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
    }

    /** Return the list of available scripts known to ScriptManager. */
    private JsonObject scriptHostListScripts() {
        java.util.List<String> names = ScriptManager.getScripts();
        JsonObject result = new JsonObject();
        result.add("scripts", GSON.toJsonTree(names.toArray(new String[0])));
        return result;
    }

    /**
     * Handle a status update pushed by the TypeScript runtime.
     *
     * Expected params:
     * <pre>{ "script": "name", "status": "running|stopped|error", "error": "msg" }</pre>
     */
    private JsonObject scriptHostScriptStatus(JsonObject params) {
        String script = params.has("script") ? params.get("script").getAsString() : null;
        String status  = params.has("status") ? params.get("status").getAsString() : "idle";
        String error   = params.has("error")  ? params.get("error").getAsString()  : null;

        if (script != null) {
            ScriptManager.ScriptStatus st = switch (status) {
                case "running" -> ScriptManager.ScriptStatus.RUNNING;
                case "error"   -> ScriptManager.ScriptStatus.ERROR;
                default        -> ScriptManager.ScriptStatus.IDLE;
            };
            ScriptManager.setStatus(script, st, error);
            LOGGER.debug("[MessageRouter] Script status update: {} → {}", script, status);
        }
        return new JsonObject();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Response helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void sendOk(WebSocket conn, String id, JsonObject result) {
        JsonObject resp = new JsonObject();
        if (id != null) resp.addProperty("id", id);
        resp.addProperty("ok", true);
        resp.add("result", result != null ? result : new JsonObject());
        wsServer.send(conn, GSON.toJson(resp));
    }

    private void sendError(WebSocket conn, String id, String error) {
        JsonObject resp = new JsonObject();
        if (id != null) resp.addProperty("id", id);
        resp.addProperty("ok", false);
        resp.addProperty("error", error != null ? error : "Unknown error");
        wsServer.send(conn, GSON.toJson(resp));
    }
}
