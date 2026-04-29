package com.marco.mcts.client.gui;

import com.google.gson.JsonObject;
import com.marco.mcts.McTsMod;
import com.marco.mcts.ScriptManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * In-game script manager screen.
 *
 * Accessible via:
 *   – Mods menu → mc-typescript → Config
 *   – Hotkey K (replaces old ScriptMenuScreen)
 *   – /tsmacro menu
 *
 * Shows all .ts/.js scripts from the scripts directory with per-entry
 * Run / Stop / Reload buttons and a live status indicator.
 */
public class TsMacroScreen extends Screen {

    private final Screen parent;
    private ScriptListWidget scriptList;

    public TsMacroScreen(Screen parent) {
        super(Component.literal("mc-typescript – Script Manager"));
        this.parent = parent;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Screen lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        // Scrollable script list – covers most of the screen
        scriptList = addWidget(new ScriptListWidget(
                this.minecraft, this.width, this.height, 32, this.height - 40, 26));
        scriptList.refreshScripts();

        int btnY = this.height - 28;
        int cx   = this.width / 2;

        // Refresh button
        addRenderableWidget(Button.builder(
                Component.literal("Aktualisieren"),
                btn -> {
                    ScriptManager.refresh();
                    scriptList.refreshScripts();
                }
        ).pos(cx - 162, btnY).size(96, 20).build());

        // Open folder button
        addRenderableWidget(Button.builder(
                Component.literal("Ordner öffnen"),
                btn -> openScriptsFolder()
        ).pos(cx - 58, btnY).size(96, 20).build());

        // Close button
        addRenderableWidget(Button.builder(
                Component.literal("Schließen"),
                btn -> onClose()
        ).pos(cx + 46, btnY).size(96, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        // Render list before other widgets so it appears behind the bottom bar
        scriptList.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        if (ScriptManager.getScripts().isEmpty()) {
            graphics.drawCenteredString(this.font,
                    Component.literal("§7Keine Skripte gefunden – lege .ts-Dateien in den Ordner:"),
                    this.width / 2, this.height / 2 - 20, 0xFFFFFF);
            if (ScriptManager.getScriptsDir() != null) {
                graphics.drawCenteredString(this.font,
                        Component.literal("§8" + ScriptManager.getScriptsDir()),
                        this.width / 2, this.height / 2 - 8, 0xFFFFFF);
            }
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private void openScriptsFolder() {
        if (ScriptManager.getScriptsDir() != null) {
            try {
                net.minecraft.Util.getPlatform().openFile(ScriptManager.getScriptsDir().toFile());
            } catch (Exception e) {
                McTsMod.LOGGER.warn("[TsMacroScreen] Could not open folder: {}", e.getMessage());
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Inner list widget
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Scrollable list of scripts.
     * Each row shows the script name, status indicator and three action buttons.
     */
    class ScriptListWidget extends AbstractSelectionList<ScriptListWidget.ScriptEntry> {

        ScriptListWidget(Minecraft mc, int width, int height, int y0, int y1, int itemHeight) {
            super(mc, width, height, y0, y1, itemHeight);
        }

        /** Rebuild the entry list from the current ScriptManager state. */
        void refreshScripts() {
            clearEntries();
            for (String name : ScriptManager.getScripts()) {
                addEntry(new ScriptEntry(name));
            }
        }

        @Override
        public int getRowWidth() {
            return TsMacroScreen.this.width - 20;
        }

        @Override
        protected int getScrollbarPosition() {
            return TsMacroScreen.this.width - 6;
        }

        // ──────────────────────────────────────────────────────────────────────
        // List entry
        // ──────────────────────────────────────────────────────────────────────

        class ScriptEntry extends AbstractSelectionList.Entry<ScriptEntry> {

            private final String name;
            private final Button btnRun;
            private final Button btnStop;
            private final Button btnReload;

            ScriptEntry(String name) {
                this.name = name;
                this.btnRun = Button.builder(Component.literal("Run"),
                        b -> doRun()).size(40, 16).build();
                this.btnStop = Button.builder(Component.literal("Stop"),
                        b -> doStop()).size(40, 16).build();
                this.btnReload = Button.builder(Component.literal("Reload"),
                        b -> doReload()).size(52, 16).build();
            }

            @Override
            public void render(GuiGraphics g, int idx, int top, int left,
                               int width, int height,
                               int mx, int my, boolean hovered, float pt) {

                // Script name
                g.drawString(TsMacroScreen.this.font, name, left + 4, top + 5, 0xFFFFFF);

                // Status indicator
                ScriptManager.ScriptStatus st = ScriptManager.getStatus(name);
                String stLabel;
                int    stColor;
                switch (st) {
                    case RUNNING -> { stLabel = " [läuft]";  stColor = 0x55FF55; }
                    case ERROR   -> { stLabel = " [Fehler]"; stColor = 0xFF5555; }
                    default      -> { stLabel = " [idle]";   stColor = 0x888888; }
                }
                int nameW = TsMacroScreen.this.font.width(name);
                g.drawString(TsMacroScreen.this.font, stLabel, left + 4 + nameW, top + 5, stColor);

                // Short error message (if any)
                if (st == ScriptManager.ScriptStatus.ERROR) {
                    String err = ScriptManager.getErrorMessage(name);
                    if (!err.isEmpty()) {
                        String display = err.length() > 50 ? err.substring(0, 47) + "…" : err;
                        g.drawString(TsMacroScreen.this.font, display, left + 4, top + 15, 0xFF8888);
                    }
                }

                // Position and render the three action buttons (right-aligned)
                int bx = left + width - 140;
                btnRun.setX(bx);        btnRun.setY(top + 4);
                btnStop.setX(bx + 44);  btnStop.setY(top + 4);
                btnReload.setX(bx + 88); btnReload.setY(top + 4);

                btnRun.render(g, mx, my, pt);
                btnStop.render(g, mx, my, pt);
                btnReload.render(g, mx, my, pt);
            }

            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                if (btnRun.mouseClicked(mx, my, button))    return true;
                if (btnStop.mouseClicked(mx, my, button))   return true;
                if (btnReload.mouseClicked(mx, my, button)) return true;
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal(name);
            }

            // ── Script actions ──────────────────────────────────────────────

            private void doRun() {
                if (McTsMod.getEventBroadcaster() != null) {
                    JsonObject d = new JsonObject();
                    d.addProperty("script", name);
                    McTsMod.getEventBroadcaster().broadcast("runScript", d);
                    ScriptManager.setStatus(name, ScriptManager.ScriptStatus.RUNNING, null);
                }
            }

            private void doStop() {
                if (McTsMod.getEventBroadcaster() != null) {
                    McTsMod.getEventBroadcaster().broadcastStopScript(name);
                    ScriptManager.setStatus(name, ScriptManager.ScriptStatus.IDLE, null);
                }
            }

            private void doReload() {
                if (McTsMod.getEventBroadcaster() != null) {
                    McTsMod.getEventBroadcaster().broadcastReloadScript(name);
                    ScriptManager.setStatus(name, ScriptManager.ScriptStatus.RUNNING, null);
                }
            }
        }
    }
}
