package com.marco.mcts.actions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Handles player inventory management: listing items, selecting hotbar slots,
 * swapping, dropping, and finding items.
 */
public class InventoryActions {

    private static final Gson GSON = new Gson();

    private static Minecraft mc() {
        return Minecraft.getInstance();
    }

    /** List all items in the player's inventory. */
    public JsonObject list() {
        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        Inventory inv = player.getInventory();
        JsonArray items = new JsonArray();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                items.add(itemToJson(stack, i));
            }
        }

        JsonObject result = new JsonObject();
        result.add("items", items);
        return result;
    }

    /**
     * Select a hotbar slot (0–8).
     * Params: { "slot": 3 }
     */
    public JsonObject selectHotbar(JsonObject params) {
        int slot = params.get("slot").getAsInt();
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("Slot must be 0–8");

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        player.getInventory().selected = slot;
        return new JsonObject();
    }

    /**
     * Swap two inventory slots.
     * Params: { "slotA": 0, "slotB": 5 }
     */
    public JsonObject swap(JsonObject params) {
        int slotA = params.get("slotA").getAsInt();
        int slotB = params.get("slotB").getAsInt();

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        // Use the click packet to perform the swap server-side
        mc().gameMode.handleInventoryMouseClick(
            player.containerMenu.containerId,
            slotA, slotB,
            net.minecraft.world.inventory.ClickType.SWAP,
            player
        );
        return new JsonObject();
    }

    /**
     * Drop items from a slot.
     * Params: { "slot": 3, "all": false }
     */
    public JsonObject drop(JsonObject params) {
        int slot = params.get("slot").getAsInt();
        boolean all = params.has("all") && params.get("all").getAsBoolean();

        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        mc().gameMode.handleInventoryMouseClick(
            player.containerMenu.containerId,
            slot,
            all ? 1 : 0,
            net.minecraft.world.inventory.ClickType.THROW,
            player
        );
        return new JsonObject();
    }

    /**
     * Find the first slot containing an item by name.
     * Params: { "name": "minecraft:diamond" }
     * Returns: { "slot": 5 } or { "slot": -1 } if not found
     */
    public JsonObject findItem(JsonObject params) {
        String name = params.get("name").getAsString();
        LocalPlayer player = mc().player;
        if (player == null) throw new IllegalStateException("Player not available");

        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                String itemName = net.minecraft.core.registries.BuiltInRegistries.ITEM
                    .getKey(stack.getItem()).toString();
                if (itemName.equals(name)) {
                    JsonObject result = new JsonObject();
                    result.addProperty("slot", i);
                    return result;
                }
            }
        }

        JsonObject result = new JsonObject();
        result.addProperty("slot", -1);
        return result;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helper
    // ──────────────────────────────────────────────────────────────────────────

    /** Convert an ItemStack to a JSON object matching the TS Item type. */
    public static JsonObject itemToJson(ItemStack stack, int slot) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", net.minecraft.core.registries.BuiltInRegistries.ITEM
            .getKey(stack.getItem()).toString());
        obj.addProperty("displayName", stack.getDisplayName().getString());
        obj.addProperty("count", stack.getCount());
        obj.addProperty("slot", slot);
        if (stack.hasTag()) {
            obj.addProperty("nbt", stack.getTag().toString());
        }
        return obj;
    }
}
