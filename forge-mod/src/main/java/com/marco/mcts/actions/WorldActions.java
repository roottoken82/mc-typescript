package com.marco.mcts.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

/**
 * Handles world-query actions: block lookup, raycasting, entity search.
 * Also provides static helpers for player info (used by MessageRouter for player.* actions).
 */
public class WorldActions {

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Static player info helpers (for MessageRouter player.* routing)
    // ──────────────────────────────────────────────────────────────────────────

    public static JsonObject playerPos() {
        LocalPlayer p = mc().player;
        if (p == null) throw new IllegalStateException("Player not available");
        JsonObject r = new JsonObject();
        r.addProperty("x", p.getX());
        r.addProperty("y", p.getY());
        r.addProperty("z", p.getZ());
        return r;
    }

    public static JsonObject playerHealth() {
        LocalPlayer p = mc().player;
        if (p == null) throw new IllegalStateException("Player not available");
        JsonObject r = new JsonObject();
        r.addProperty("health", p.getHealth());
        return r;
    }

    public static JsonObject playerHunger() {
        LocalPlayer p = mc().player;
        if (p == null) throw new IllegalStateException("Player not available");
        JsonObject r = new JsonObject();
        r.addProperty("hunger", p.getFoodData().getFoodLevel());
        return r;
    }

    public static JsonObject playerYaw() {
        LocalPlayer p = mc().player;
        if (p == null) throw new IllegalStateException("Player not available");
        JsonObject r = new JsonObject();
        r.addProperty("yaw", p.getYRot());
        return r;
    }

    public static JsonObject playerPitch() {
        LocalPlayer p = mc().player;
        if (p == null) throw new IllegalStateException("Player not available");
        JsonObject r = new JsonObject();
        r.addProperty("pitch", p.getXRot());
        return r;
    }

    public static JsonObject playerStatus() {
        LocalPlayer p = mc().player;
        if (p == null) throw new IllegalStateException("Player not available");
        JsonObject r = new JsonObject();
        r.addProperty("x", p.getX());
        r.addProperty("y", p.getY());
        r.addProperty("z", p.getZ());
        r.addProperty("yaw", p.getYRot());
        r.addProperty("pitch", p.getXRot());
        r.addProperty("health", p.getHealth());
        r.addProperty("hunger", p.getFoodData().getFoodLevel());
        r.addProperty("gameMode", mc().gameMode != null
            ? mc().gameMode.getPlayerMode().getName() : "unknown");
        r.addProperty("dimension", p.level().dimension().location().toString());
        return r;
    }

    public static JsonObject playerGameMode() {
        JsonObject r = new JsonObject();
        r.addProperty("gameMode", mc().gameMode != null
            ? mc().gameMode.getPlayerMode().getName() : "unknown");
        return r;
    }

    public static JsonObject playerDimension() {
        LocalPlayer p = mc().player;
        if (p == null) throw new IllegalStateException("Player not available");
        JsonObject r = new JsonObject();
        r.addProperty("dimension", p.level().dimension().location().toString());
        return r;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Instance world queries
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Get block info at a position.
     * Params: { "x": 0, "y": 64, "z": 0 }
     */
    public JsonObject getBlock(JsonObject params) {
        int x = params.get("x").getAsInt();
        int y = params.get("y").getAsInt();
        int z = params.get("z").getAsInt();

        Level level = mc().level;
        if (level == null) throw new IllegalStateException("Level not available");

        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level.getBlockState(pos);

        JsonObject blockObj = new JsonObject();
        blockObj.addProperty("name", BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());

        JsonObject stateObj = new JsonObject();
        for (Map.Entry<Property<?>, Comparable<?>> entry : state.getValues().entrySet()) {
            stateObj.addProperty(entry.getKey().getName(), entry.getValue().toString());
        }
        blockObj.add("state", stateObj);

        JsonObject posObj = new JsonObject();
        posObj.addProperty("x", x);
        posObj.addProperty("y", y);
        posObj.addProperty("z", z);
        blockObj.add("pos", posObj);

        JsonObject result = new JsonObject();
        result.add("block", blockObj);
        return result;
    }

    /**
     * Ray-cast from the player's eye position.
     * Params: { "maxDist": 4.5 }
     */
    public JsonObject raycast(JsonObject params) {
        double maxDist = params.has("maxDist") ? params.get("maxDist").getAsDouble() : 4.5;

        LocalPlayer player = mc().player;
        Level level = mc().level;
        if (player == null || level == null) throw new IllegalStateException("Player or level not available");

        HitResult hit = mc().hitResult;
        JsonObject result = new JsonObject();

        if (hit == null || hit.getType() == HitResult.Type.MISS) {
            result.addProperty("hit", false);
        } else if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bhr) {
            result.addProperty("hit", true);
            BlockPos bPos = bhr.getBlockPos();
            BlockState bState = level.getBlockState(bPos);

            JsonObject blockObj = new JsonObject();
            blockObj.addProperty("name", BuiltInRegistries.BLOCK.getKey(bState.getBlock()).toString());
            blockObj.add("state", new JsonObject());
            JsonObject bPosObj = new JsonObject();
            bPosObj.addProperty("x", bPos.getX());
            bPosObj.addProperty("y", bPos.getY());
            bPosObj.addProperty("z", bPos.getZ());
            blockObj.add("pos", bPosObj);
            result.add("block", blockObj);

            Vec3 loc = bhr.getLocation();
            JsonObject hitPos = new JsonObject();
            hitPos.addProperty("x", loc.x);
            hitPos.addProperty("y", loc.y);
            hitPos.addProperty("z", loc.z);
            result.add("pos", hitPos);
        } else {
            result.addProperty("hit", true);
        }
        return result;
    }

    /**
     * Return all entities within a given radius around the player.
     * Params: { "radius": 10 }
     */
    public JsonObject entitiesNear(JsonObject params) {
        double radius = params.get("radius").getAsDouble();

        LocalPlayer player = mc().player;
        Level level = mc().level;
        if (player == null || level == null) throw new IllegalStateException("Player or level not available");

        AABB box = player.getBoundingBox().inflate(radius);
        List<Entity> entities = level.getEntities(player, box);

        JsonArray arr = new JsonArray();
        for (Entity e : entities) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", e.getId());
            obj.addProperty("type", BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).toString());
            obj.addProperty("name", e.getDisplayName().getString());
            JsonObject ePos = new JsonObject();
            ePos.addProperty("x", e.getX());
            ePos.addProperty("y", e.getY());
            ePos.addProperty("z", e.getZ());
            obj.add("pos", ePos);
            if (e instanceof net.minecraft.world.entity.LivingEntity le) {
                obj.addProperty("health", le.getHealth());
            }
            arr.add(obj);
        }

        JsonObject result = new JsonObject();
        result.add("entities", arr);
        return result;
    }

    /**
     * Find the nearest block of a given type within a radius.
     * Params: { "name": "minecraft:chest", "radius": 16 }
     */
    public JsonObject findBlock(JsonObject params) {
        String name   = params.get("name").getAsString();
        int radius    = params.has("radius") ? params.get("radius").getAsInt() : 16;

        LocalPlayer player = mc().player;
        Level level = mc().level;
        if (player == null || level == null) throw new IllegalStateException("Player or level not available");

        BlockPos center = player.blockPosition();
        BlockPos found  = null;
        double minDist  = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos check = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(check);
                    String blockName = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
                    if (blockName.equals(name)) {
                        double dist = check.distSqr(center);
                        if (dist < minDist) {
                            minDist = dist;
                            found   = check;
                        }
                    }
                }
            }
        }

        JsonObject result = new JsonObject();
        if (found != null) {
            JsonObject pos = new JsonObject();
            pos.addProperty("x", found.getX());
            pos.addProperty("y", found.getY());
            pos.addProperty("z", found.getZ());
            result.add("pos", pos);
        } else {
            result.add("pos", JsonNull.INSTANCE);
        }
        return result;
    }

    /** Return the player's spawn point. */
    public JsonObject spawnPoint() {
        LocalPlayer p = mc().player;
        if (p == null) throw new IllegalStateException("Player not available");
        BlockPos spawn = p.getRespawnPosition();
        JsonObject result = new JsonObject();
        if (spawn != null) {
            result.addProperty("x", spawn.getX());
            result.addProperty("y", spawn.getY());
            result.addProperty("z", spawn.getZ());
        } else {
            result.addProperty("x", 0);
            result.addProperty("y", 64);
            result.addProperty("z", 0);
        }
        return result;
    }
}
