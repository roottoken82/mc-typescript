package com.marco.mcts.actions;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Handles mouse-click and entity-interaction actions.
 */
public class InteractionActions {

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** Perform a single left-click attack in the look direction. */
    public JsonObject attack() {
        mc().startAttack();
        return new JsonObject();
    }

    /** Perform a single right-click use in the look direction. */
    public JsonObject use() {
        mc().gameMode.useItem(mc().player, net.minecraft.world.InteractionHand.MAIN_HAND);
        return new JsonObject();
    }

    /** Start holding down left-click (continuous block breaking). */
    public JsonObject startBreaking() {
        // Trigger the key-hold state in the game options
        mc().options.keyAttack.setDown(true);
        return new JsonObject();
    }

    /** Stop holding down left-click. */
    public JsonObject stopBreaking() {
        mc().options.keyAttack.setDown(false);
        return new JsonObject();
    }

    /**
     * Place a block on the given face of the block at the given position.
     * Params: { "x": 0, "y": 64, "z": 0, "face": "up" }
     */
    public JsonObject placeBlockAt(JsonObject params) {
        int x = params.get("x").getAsInt();
        int y = params.get("y").getAsInt();
        int z = params.get("z").getAsInt();
        String faceStr = params.has("face") ? params.get("face").getAsString() : "up";

        Direction face = switch (faceStr.toLowerCase()) {
            case "down"  -> Direction.DOWN;
            case "north" -> Direction.NORTH;
            case "south" -> Direction.SOUTH;
            case "east"  -> Direction.EAST;
            case "west"  -> Direction.WEST;
            default      -> Direction.UP;
        };

        BlockPos pos = new BlockPos(x, y, z);
        Vec3 hitVec = Vec3.atCenterOf(pos).add(Vec3.atLowerCornerOf(face.getNormal()).scale(0.5));
        BlockHitResult hitResult = new BlockHitResult(hitVec, face, pos, false);

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        mc().gameMode.useItemOn(player, net.minecraft.world.InteractionHand.MAIN_HAND, hitResult);
        return new JsonObject();
    }

    /**
     * Use the active item on an entity.
     * Params: { "entityId": 42 }
     */
    public JsonObject useItemOnEntity(JsonObject params) {
        int entityId = params.get("entityId").getAsInt();
        Entity target = mc().level != null ? mc().level.getEntity(entityId) : null;
        if (target == null) throw new IllegalArgumentException("Entity not found: " + entityId);

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        mc().gameMode.interactWith(player, target, net.minecraft.world.InteractionHand.MAIN_HAND);
        return new JsonObject();
    }

    /**
     * Attack (left-click) the given entity.
     * Params: { "entityId": 42 }
     */
    public JsonObject attackEntity(JsonObject params) {
        int entityId = params.get("entityId").getAsInt();
        Entity target = mc().level != null ? mc().level.getEntity(entityId) : null;
        if (target == null) throw new IllegalArgumentException("Entity not found: " + entityId);

        mc().gameMode.attack(mc().player, target);
        return new JsonObject();
    }
}
