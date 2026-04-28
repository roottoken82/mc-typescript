package com.marco.mcts.client;

import com.marco.mcts.McTsMod;
import com.marco.mcts.client.gui.ScriptMenuScreen;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

/**
 * Registers configurable hotkeys for mc-typescript.
 *
 * Default bindings:
 *   K          – Open script menu
 *   Numpad 1–9 – Quick-run script at slot 1–9
 */
public class KeyBindings {

    private static final String CATEGORY = "key.categories.mcts";

    /** Menu key (default: K) */
    public static KeyMapping KEY_MENU;
    /** Quick-run slots 1–9 (default: Numpad 1–9) */
    public static final KeyMapping[] KEY_QUICK_RUN = new KeyMapping[9];

    /** Register all key mappings on the mod event bus. */
    public static void register() {
        KEY_MENU = new KeyMapping("key.mcts.menu", GLFW.GLFW_KEY_K, CATEGORY);

        int[] numpadKeys = {
            GLFW.GLFW_KEY_KP_1, GLFW.GLFW_KEY_KP_2, GLFW.GLFW_KEY_KP_3,
            GLFW.GLFW_KEY_KP_4, GLFW.GLFW_KEY_KP_5, GLFW.GLFW_KEY_KP_6,
            GLFW.GLFW_KEY_KP_7, GLFW.GLFW_KEY_KP_8, GLFW.GLFW_KEY_KP_9
        };
        for (int i = 0; i < 9; i++) {
            KEY_QUICK_RUN[i] = new KeyMapping(
                "key.mcts.quickrun." + (i + 1), numpadKeys[i], CATEGORY);
        }

        // Register the key handler on the FORGE bus
        MinecraftForge.EVENT_BUS.register(new KeyInputHandler());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Key handler (inner class for EVENT_BUS registration)
    // ──────────────────────────────────────────────────────────────────────────

    public static class KeyInputHandler {

        @SubscribeEvent
        public void onKeyInput(InputEvent.Key event) {
            // Menu key
            if (KEY_MENU != null && KEY_MENU.consumeClick()) {
                net.minecraft.client.Minecraft.getInstance()
                    .execute(() -> net.minecraft.client.Minecraft.getInstance()
                        .setScreen(new ScriptMenuScreen()));
            }

            // Quick-run keys
            for (int i = 0; i < 9; i++) {
                if (KEY_QUICK_RUN[i] != null && KEY_QUICK_RUN[i].consumeClick()) {
                    if (McTsMod.getEventBroadcaster() != null) {
                        McTsMod.getEventBroadcaster().broadcastHotkey("NUMPAD_" + (i + 1));
                    }
                }
            }
        }
    }
}
