package com.marco.mcts.events;

import com.marco.mcts.McTsMod;
import com.marco.mcts.network.EventBroadcaster;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listens for incoming chat messages and broadcasts them to the TypeScript runtime.
 *
 * Parses standard vanilla chat format "<username> message" to extract the sender.
 */
public class ChatListener {

    private static final Logger LOGGER = LogManager.getLogger("mcts.ChatListener");

    /** Matches the vanilla chat format: <PlayerName> message */
    private static final Pattern CHAT_PATTERN = Pattern.compile("^<([^>]+)>\\s+(.+)$");

    private static EventBroadcaster broadcaster;

    public static void setEventBroadcaster(EventBroadcaster b) {
        broadcaster = b;
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (!McTsMod.isModEnabled()) return;
        if (broadcaster == null) return;

        Component message = event.getMessage();
        String text = message.getString();

        Matcher matcher = CHAT_PATTERN.matcher(text);
        if (matcher.matches()) {
            String user = matcher.group(1);
            String msg  = matcher.group(2);
            broadcaster.broadcastChat(user, msg);
        }
        // System messages (not matching <user>) are ignored unless you add a 'systemMessage' event.
    }
}
