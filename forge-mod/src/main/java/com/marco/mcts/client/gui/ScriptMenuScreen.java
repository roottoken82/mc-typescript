package com.marco.mcts.client.gui;

import com.marco.mcts.McTsMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * In-game script selection screen.
 * Opens when the player presses K or uses /mcts menu.
 *
 * The available script list is populated from the TS runtime via WebSocket.
 * For now, a static placeholder list is shown.
 */
public class ScriptMenuScreen extends Screen {

    /** Scripts available for selection – populated dynamically. */
    private static final List<String> SCRIPTS = new ArrayList<>();

    public ScriptMenuScreen() {
        super(Component.literal("mc-typescript – Script Menu"));
    }

    /** Called by the ScriptHost WS event to update the list. */
    public static void setScripts(List<String> scripts) {
        SCRIPTS.clear();
        SCRIPTS.addAll(scripts);
    }

    @Override
    protected void init() {
        int btnWidth  = 160;
        int btnHeight = 20;
        int startY    = 40;
        int spacing   = 24;

        int centerX = this.width / 2;

        if (SCRIPTS.isEmpty()) {
            // Placeholder when no scripts are loaded yet
            addRenderableWidget(Button.builder(
                Component.literal("(No scripts loaded – start ts-library runtime)"),
                btn -> {}
            ).pos(centerX - btnWidth, startY).size(btnWidth * 2, btnHeight).build());
        } else {
            for (int i = 0; i < SCRIPTS.size() && i < 9; i++) {
                final String scriptName = SCRIPTS.get(i);
                addRenderableWidget(Button.builder(
                    Component.literal((i + 1) + ". " + scriptName),
                    btn -> runScript(scriptName)
                ).pos(centerX - btnWidth / 2, startY + i * spacing).size(btnWidth, btnHeight).build());
            }
        }

        // Close button
        addRenderableWidget(Button.builder(
            Component.literal("Close"),
            btn -> this.onClose()
        ).pos(centerX - 40, this.height - 30).size(80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void runScript(String name) {
        if (McTsMod.getEventBroadcaster() != null) {
            com.google.gson.JsonObject data = new com.google.gson.JsonObject();
            data.addProperty("script", name);
            McTsMod.getEventBroadcaster().broadcast("runScript", data);
        }
        this.onClose();
    }
}
