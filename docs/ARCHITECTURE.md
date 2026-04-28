# Architecture – mc-typescript

## Overview

mc-typescript is a **client-side only** system that gives you TypeScript control over your Minecraft Java client. No server-side modifications are needed – it works with any Vanilla, Paper, or Spigot server (including Aternos).

```
┌─────────────────────────────────────────────────────────────────────────┐
│  Your PC                                                                │
│                                                                         │
│  ┌──────────────────────────────┐    WebSocket    ┌─────────────────┐  │
│  │  Minecraft Client (Forge)    │  ◄──────────►  │  Node.js TS     │  │
│  │                              │    :8765        │  Runtime        │  │
│  │  WSServer  (Java-WS :8765)   │                 │  ScriptHost     │  │
│  │  MessageRouter               │                 │                 │  │
│  │  EventBroadcaster            │                 │  mc-typescript  │  │
│  │  Actions (Chat/Move/Inv/...) │                 │  (NPM library)  │  │
│  │  Commands (/mcts ...)        │                 │                 │  │
│  │  Hotkeys (K, Numpad 1-9)     │                 │  scripts/*.ts   │  │
│  │  ScriptMenuScreen (GUI)      │                 │                 │  │
│  └──────────────────────────────┘                 └─────────────────┘  │
│                   ▲                                                     │
│                   │ Minecraft Protocol                                  │
│                   ▼                                                     │
│  ┌──────────────────────────────┐                                       │
│  │  Aternos Server (Vanilla)    │                                       │
│  │  (untouched, no mods)        │                                       │
│  └──────────────────────────────┘                                       │
└─────────────────────────────────────────────────────────────────────────┘
```

## Components

### 1. Forge Mod (`forge-mod/`)

The "Body" – runs inside your Minecraft client.

| Component | Role |
|-----------|------|
| `McTsMod.java` | Entry point, wires everything together |
| `WSServer.java` | Java-WebSocket server on port 8765 |
| `MessageRouter.java` | Parses JSON requests and dispatches to action handlers |
| `EventBroadcaster.java` | Sends game events to all connected TS clients |
| `actions/` | One handler class per action group (chat, movement, inventory, …) |
| `commands/McTsCommand.java` | `/mcts enable|run|stop|status|menu|reload` |
| `client/KeyBindings.java` | Registers configurable hotkeys in MC Controls |
| `client/gui/ScriptMenuScreen.java` | In-game script selection UI |
| `events/` | Forge event listeners (chat, tick, damage, player join/leave) |

### 2. TypeScript Library (`ts-library/`)

The "Brain" – runs in Node.js on your PC.

| Component | Role |
|-----------|------|
| `WSClient.ts` | Low-level WebSocket client with request/response correlation |
| `EventBus.ts` | Typed event emitter for game events |
| `api/*.ts` | High-level API modules (player, movement, interact, inventory, …) |
| `index.ts` | Exports the `mc` object and all public types |
| `runtime/ScriptHost.ts` | Loads scripts, manages processes, watches for hot-reload |

### 3. Scripts (`scripts/`)

User TypeScript scripts that import `mc-typescript` and define behaviour.  
The ScriptHost loads and runs them as child processes.

## Data Flow

### Action (TS → Mod)

```
User script calls mc.chat.send("hello")
  → WSClient.request("chat.send", { message: "hello" })
     → WebSocket frame: { "id": "x1", "action": "chat.send", "params": { "message": "hello" } }
        → WSServer receives → MessageRouter.route()
           → Minecraft.getInstance().execute(() → ChatActions.sendChat())
              → player.chat("hello")
                 → { "id": "x1", "ok": true, "result": {} }
                    → WSClient resolves the Promise
                       → mc.chat.send() resolves
```

### Event (Mod → TS)

```
Player receives chat from "Marco": "hello"
  → ChatListener.onChatReceived()
     → EventBroadcaster.broadcastChat("Marco", "hello")
        → WSServer.broadcast({ "event": "chat", "data": { "user": "Marco", "message": "hello" } })
           → WSClient.onMessage() → EventBus.emit("chat", "Marco", "hello")
              → User's mc.on('chat', ...) handler fires
```

## Thread Safety

All Minecraft operations are executed on the main thread via `Minecraft.getInstance().execute(Runnable)`.  
The WebSocket server runs on its own Java thread.  
The MessageRouter bridges these two contexts safely.

## Safety Toggle

The mod has a safety toggle: it only processes actions when explicitly enabled with `/mcts enable`.  
This prevents accidental activation on public servers.
