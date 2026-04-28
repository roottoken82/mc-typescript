package com.marco.mcts.actions;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * Handles player movement actions: walking, jumping, looking, sprinting, etc.
 *
 * Note: Tick-based movement (forward, back, left, right) is executed by
 * scheduling input flags for the given number of game ticks via a simple
 * state machine that the TickListener queries each tick.
 */
public class MovementActions {

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** Shared movement state – read by TickListener every tick. */
    public static final MovementState state = new MovementState();

    // ──────────────────────────────────────────────────────────────────────────

    /** Move forward for the given number of ticks. */
    public JsonObject forward(JsonObject params) {
        int ticks = params.get("ticks").getAsInt();
        state.forwardTicks = ticks;
        return new JsonObject();
    }

    /** Move backward for the given number of ticks. */
    public JsonObject back(JsonObject params) {
        int ticks = params.get("ticks").getAsInt();
        state.backTicks = ticks;
        return new JsonObject();
    }

    /** Strafe left for the given number of ticks. */
    public JsonObject left(JsonObject params) {
        int ticks = params.get("ticks").getAsInt();
        state.leftTicks = ticks;
        return new JsonObject();
    }

    /** Strafe right for the given number of ticks. */
    public JsonObject right(JsonObject params) {
        int ticks = params.get("ticks").getAsInt();
        state.rightTicks = ticks;
        return new JsonObject();
    }

    /** Perform a single jump. */
    public JsonObject jump() {
        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");
        player.jumpFromGround();
        return new JsonObject();
    }

    /** Enable or disable sneaking. Params: { "on": true } */
    public JsonObject sneak(JsonObject params) {
        boolean on = params.get("on").getAsBoolean();
        state.sneaking = on;
        return new JsonObject();
    }

    /** Enable or disable sprinting. Params: { "on": true } */
    public JsonObject sprint(JsonObject params) {
        boolean on = params.get("on").getAsBoolean();
        LocalPlayer player = mc().player;
        if (player != null) player.setSprinting(on);
        state.sprinting = on;
        return new JsonObject();
    }

    /**
     * Look at a world position.
     * Params: { "x": 0.0, "y": 64.0, "z": 0.0 }
     */
    public JsonObject lookAt(JsonObject params) {
        double tx = params.get("x").getAsDouble();
        double ty = params.get("y").getAsDouble();
        double tz = params.get("z").getAsDouble();

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        Vec3 playerEyePos = player.getEyePosition();
        double dx = tx - playerEyePos.x;
        double dy = ty - playerEyePos.y;
        double dz = tz - playerEyePos.z;

        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, dist)));

        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
        return new JsonObject();
    }

    /**
     * Set the player's look direction directly.
     * Params: { "yaw": 0.0, "pitch": 0.0 }
     */
    public JsonObject setLook(JsonObject params) {
        float yaw   = params.get("yaw").getAsFloat();
        float pitch = params.get("pitch").getAsFloat();

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yRotO = yaw;
        player.xRotO = pitch;
        return new JsonObject();
    }

    /**
     * Walk to the given coordinates (simple greedy movement – no obstacle avoidance).
     * Params: { "x": 0.0, "y": 64.0, "z": 0.0 }
     */
    public JsonObject walkTo(JsonObject params) {
        double tx = params.get("x").getAsDouble();
        double ty = params.get("y").getAsDouble();
        double tz = params.get("z").getAsDouble();

        state.walkToTarget = new double[]{ tx, ty, tz };
        return new JsonObject();
    }

    /** Stop all movement. */
    public JsonObject stop() {
        state.forwardTicks = 0;
        state.backTicks    = 0;
        state.leftTicks    = 0;
        state.rightTicks   = 0;
        state.sneaking     = false;
        state.sprinting    = false;
        state.walkToTarget = null;
        return new JsonObject();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // MovementState inner class
    // ──────────────────────────────────────────────────────────────────────────

    /** Mutable state shared between MovementActions and TickListener. */
    public static class MovementState {
        public volatile int forwardTicks = 0;
        public volatile int backTicks    = 0;
        public volatile int leftTicks    = 0;
        public volatile int rightTicks   = 0;
        public volatile boolean sneaking = false;
        public volatile boolean sprinting = false;
        /** Target for walkTo; null if no active walkTo. */
        public volatile double[] walkToTarget = null;
    }
}
