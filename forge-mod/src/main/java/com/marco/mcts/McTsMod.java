package com.marco.mcts;

import com.marco.mcts.client.KeyBindings;
import com.marco.mcts.client.gui.TsMacroScreen;
import com.marco.mcts.commands.McTsCommand;
import com.marco.mcts.commands.TsMacroCommand;
import com.marco.mcts.events.ChatListener;
import com.marco.mcts.events.EntityListener;
import com.marco.mcts.events.TickListener;
import com.marco.mcts.network.EventBroadcaster;
import com.marco.mcts.network.MessageRouter;
import com.marco.mcts.network.WSServer;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main mod class for mc-typescript.
 *
 * Starts the WebSocket server on port 8765 and wires up all event listeners,
 * commands, and key-bindings.
 */
@Mod(McTsMod.MOD_ID)
public class McTsMod {

    public static final String MOD_ID = "mcts";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    /** Whether the mod is currently enabled (safety toggle via /mcts enable) */
    private static boolean modEnabled = false;

    private static WSServer wsServer;
    private static EventBroadcaster eventBroadcaster;

    public McTsMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ChatListener());
        MinecraftForge.EVENT_BUS.register(new TickListener());
        MinecraftForge.EVENT_BUS.register(new EntityListener());

        LOGGER.info("[mc-typescript] Mod loaded. Use /mcts enable to activate.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Initialise the script manager (scans <mc>/config/mc-typescript/scripts/)
        event.enqueueWork(ScriptManager::init);

        // Start the WebSocket server and wire up the message router
        int port = 8765;
        wsServer = new WSServer(port);
        eventBroadcaster = new EventBroadcaster(wsServer);

        // MessageRouter constructor also calls wsServer.setRouter(this)
        new MessageRouter(wsServer);

        // Share the broadcaster with event listeners so they can send events
        ChatListener.setEventBroadcaster(eventBroadcaster);
        TickListener.setEventBroadcaster(eventBroadcaster);
        EntityListener.setEventBroadcaster(eventBroadcaster);

        wsServer.start();
        LOGGER.info("[mc-typescript] WebSocket server started on port {}", port);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        KeyBindings.register();

        // Register the Config button in the Mods menu (Mods → mc-typescript → Config)
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (mc, parent) -> new TsMacroScreen(parent)
            )
        );

        LOGGER.info("[mc-typescript] Key bindings and config screen registered.");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        McTsCommand.register(event.getDispatcher());
        TsMacroCommand.register(event.getDispatcher());
        LOGGER.info("[mc-typescript] Commands registered.");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Static helpers used by other classes
    // ──────────────────────────────────────────────────────────────────────────

    public static boolean isModEnabled() {
        return modEnabled;
    }

    public static void setModEnabled(boolean enabled) {
        modEnabled = enabled;
        LOGGER.info("[mc-typescript] Mod {}.", enabled ? "ENABLED" : "DISABLED");
    }

    public static WSServer getWsServer() {
        return wsServer;
    }

    public static EventBroadcaster getEventBroadcaster() {
        return eventBroadcaster;
    }
}
