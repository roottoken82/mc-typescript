package com.marco.mcts.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Handles container (chest, barrel, furnace, etc.) interactions.
 * The player must be within reach of the block when openAt is called.
 */
public class ContainerActions {

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /**
     * Open the container at the given position by simulating a right-click.
     * Params: { "x": 0, "y": 64, "z": 0 }
     */
    public JsonObject openAt(JsonObject params) {
        int x = params.get("x").getAsInt();
        int y = params.get("y").getAsInt();
        int z = params.get("z").getAsInt();

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        BlockPos pos = new BlockPos(x, y, z);

        // Use InteractionActions to right-click the block
        net.minecraft.core.Direction face = net.minecraft.core.Direction.UP;
        net.minecraft.world.phys.Vec3 hitVec = net.minecraft.world.phys.Vec3.atCenterOf(pos);
        net.minecraft.world.phys.BlockHitResult hitResult =
            new net.minecraft.world.phys.BlockHitResult(hitVec, face, pos, false);

        mc().gameMode.useItemOn(player, net.minecraft.world.InteractionHand.MAIN_HAND, hitResult);
        return new JsonObject();
    }

    /**
     * List all slots of the currently open container.
     * Empty slots are represented as null in the JSON array.
     */
    public JsonObject slots() {
        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        AbstractContainerMenu menu = player.containerMenu;
        JsonArray slots = new JsonArray();

        // Skip the first 'offset' slots which belong to the player inventory within the container screen.
        // Container slots come first in the list.
        for (int i = 0; i < menu.slots.size(); i++) {
            net.minecraft.world.inventory.Slot slot = menu.slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty()) {
                slots.add(JsonNull.INSTANCE);
            } else {
                slots.add(InventoryActions.itemToJson(stack, i));
            }
        }

        JsonObject result = new JsonObject();
        result.add("slots", slots);
        return result;
    }

    /**
     * Take items from a container slot.
     * Params: { "slot": 3, "count": 64 }
     * count = -1 means take the full stack.
     */
    public JsonObject take(JsonObject params) {
        int slot  = params.get("slot").getAsInt();
        int count = params.get("count").getAsInt(); // -1 = full stack

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        AbstractContainerMenu menu = player.containerMenu;

        if (count == -1 || count >= menu.slots.get(slot).getItem().getCount()) {
            // Left-click = take full stack
            mc().gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.PICKUP, player);
            // Left-click empty area to drop into inventory – use quickMove instead
            mc().gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, player);
        } else {
            // Right-click = take half; repeat as needed (simplified: take full then re-put remainder)
            mc().gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, player);
        }
        return new JsonObject();
    }

    /**
     * Put items from the player's inventory into the container.
     * Params: { "invSlot": 36, "containerSlot": -1, "count": -1 }
     * containerSlot = -1 → auto-stack / first empty slot
     * count = -1 → full stack
     */
    public JsonObject put(JsonObject params) {
        int invSlot       = params.get("invSlot").getAsInt();
        int containerSlot = params.get("containerSlot").getAsInt(); // -1 = auto
        // count is handled by quickMove (full stack only in this simplified impl)

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        AbstractContainerMenu menu = player.containerMenu;

        if (containerSlot == -1) {
            // Shift-click the inventory slot to auto-move it to the container
            mc().gameMode.handleInventoryMouseClick(menu.containerId, invSlot, 0, ClickType.QUICK_MOVE, player);
        } else {
            // Pick up from inventory slot, then place in container slot
            mc().gameMode.handleInventoryMouseClick(menu.containerId, invSlot, 0, ClickType.PICKUP, player);
            mc().gameMode.handleInventoryMouseClick(menu.containerId, containerSlot, 0, ClickType.PICKUP, player);
        }
        return new JsonObject();
    }

    /**
     * Shift-click a container slot (quick-move to/from player inventory).
     * Params: { "slot": 3 }
     */
    public JsonObject quickMove(JsonObject params) {
        int slot = params.get("slot").getAsInt();

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        AbstractContainerMenu menu = player.containerMenu;
        mc().gameMode.handleInventoryMouseClick(menu.containerId, slot, 0, ClickType.QUICK_MOVE, player);
        return new JsonObject();
    }

    /** Close the currently open container. */
    public JsonObject close() {
        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        player.closeContainer();
        return new JsonObject();
    }
}
