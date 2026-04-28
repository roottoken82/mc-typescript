package com.marco.mcts.network;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Java-WebSocket server that listens on port 8765.
 * Incoming JSON messages are forwarded to the {@link MessageRouter}.
 * Outgoing events are sent to all connected clients via {@link #broadcast(String)}.
 */
public class WSServer extends WebSocketServer {

    private static final Logger LOGGER = LogManager.getLogger("mcts.WSServer");

    private final Set<WebSocket> clients = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private MessageRouter router;

    public WSServer(int port) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
        setConnectionLostTimeout(60);
    }

    /** Set the message router after construction (avoids circular dependency). */
    public void setRouter(MessageRouter router) {
        this.router = router;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WebSocketServer callbacks
    // ──────────────────────────────────────────────────────────────────────────

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        LOGGER.info("[WSServer] Client connected: {}", conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        LOGGER.info("[WSServer] Client disconnected: {} (code={}, reason={})", conn.getRemoteSocketAddress(), code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOGGER.debug("[WSServer] Received: {}", message);
        if (router != null) {
            router.route(conn, message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOGGER.error("[WSServer] Error on connection {}: {}", conn != null ? conn.getRemoteSocketAddress() : "null", ex.getMessage());
    }

    @Override
    public void onStart() {
        LOGGER.info("[WSServer] Listening on port {}", getPort());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /** Send a JSON string to all connected TypeScript clients. */
    public void broadcast(String json) {
        for (WebSocket client : clients) {
            if (client.isOpen()) {
                client.send(json);
            }
        }
    }

    /** Send a JSON string to a specific client. */
    public void send(WebSocket conn, String json) {
        if (conn.isOpen()) {
            conn.send(json);
        }
    }

    /** Number of currently connected clients. */
    public int getClientCount() {
        return clients.size();
    }
}
