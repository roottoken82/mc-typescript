package com.marco.mcts.client.gui;

import com.marco.mcts.client.KeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * In-game hotkey configuration screen (placeholder).
 *
 * Full re-binding is handled via Minecraft's built-in Controls screen
 * (Options → Controls → mc-typescript category).
 * This screen simply lists the current bindings for reference.
 */
public class HotkeyConfigScreen extends Screen {

    private final Screen parent;

    public HotkeyConfigScreen(Screen parent) {
        super(Component.literal("mc-typescript – Hotkey Configuration"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        // Back button
        addRenderableWidget(Button.builder(
            Component.literal("Back"),
            btn -> this.minecraft.setScreen(parent)
        ).pos(this.width / 2 - 40, this.height - 30).size(80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        int y = 40;
        graphics.drawString(this.font, "Current key bindings:", 20, y, 0xAAAAAA);
        y += 15;

        // Menu key
        if (KeyBindings.KEY_MENU != null) {
            graphics.drawString(this.font,
                "Menu:     " + KeyMapping.createNameSupplier(KeyBindings.KEY_MENU.getName()).get().getString(),
                20, y, 0xFFFFFF);
            y += 12;
        }

        // Quick-run keys
        for (int i = 0; i < KeyBindings.KEY_QUICK_RUN.length; i++) {
            KeyMapping km = KeyBindings.KEY_QUICK_RUN[i];
            if (km != null) {
                graphics.drawString(this.font,
                    "Quick-run " + (i + 1) + ":  "
                        + KeyMapping.createNameSupplier(km.getName()).get().getString(),
                    20, y, 0xFFFFFF);
                y += 12;
            }
        }

        y += 10;
        graphics.drawString(this.font,
            "Tip: Go to Options > Controls > mc-typescript to rebind keys.",
            20, y, 0x888888);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
