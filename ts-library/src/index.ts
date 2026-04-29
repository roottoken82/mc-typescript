import { WSClient } from './client/WSClient';
import { EventBus } from './client/EventBus';
import { PlayerAPI } from './api/player';
import { MovementAPI } from './api/movement';
import { InteractAPI } from './api/interact';
import { InventoryAPI } from './api/inventory';
import { ContainerAPI } from './api/container';
import { WorldAPI } from './api/world';
import { ChatAPI } from './api/chat';
import { EventsAPI } from './api/events';
import type { McEvent, EventPayloads } from './api/events';

// ──────────────────────────────────────────────────────────────────────────────
// Internal singletons (lazy-initialised on first use)
// ──────────────────────────────────────────────────────────────────────────────

let _wsClient: WSClient | null = null;
let _eventBus: EventBus | null = null;

function getWsClient(): WSClient {
  if (!_wsClient) {
    _wsClient = new WSClient(process.env['MC_WS_URL'] ?? 'ws://localhost:8765');
  }
  return _wsClient;
}

function getEventBus(): EventBus {
  if (!_eventBus) {
    _eventBus = new EventBus();

    // Route incoming WS events to the EventBus
    getWsClient().onMessage((msg) => {
      const event = msg['event'] as string | undefined;
      if (!event) return;

      const data = (msg['data'] ?? {}) as Record<string, unknown>;

      switch (event) {
        case 'chat':
          _eventBus!.emit('chat', data['user'] as string, data['message'] as string);
          break;
        case 'hotkey':
          _eventBus!.emit('hotkey', data['key'] as string);
          break;
        case 'tick':
          _eventBus!.emit('tick');
          break;
        case 'damage':
          _eventBus!.emit('damage', data['source'] as string, data['amount'] as number);
          break;
        case 'playerJoin':
          _eventBus!.emit('playerJoin', data['name'] as string);
          break;
        case 'playerLeave':
          _eventBus!.emit('playerLeave', data['name'] as string);
          break;
        case 'death':
          _eventBus!.emit('death');
          break;
        // Script control events from the mod → ScriptHost
        case 'runScript':
          _eventBus!.emit('runScript', data['script'] as string);
          break;
        case 'stopScript':
          _eventBus!.emit('stopScript', data['script'] as string);
          break;
        case 'reloadScript':
          _eventBus!.emit('reloadScript', data['script'] as string);
          break;
        case 'stopAllScripts':
          _eventBus!.emit('stopAllScripts');
          break;
        default:
          // Unknown event – forward as-is for extensibility
          _eventBus!.emit(event, data);
      }
    });
  }
  return _eventBus;
}

// ──────────────────────────────────────────────────────────────────────────────
// Public mc object – the main entry point for user scripts
// ──────────────────────────────────────────────────────────────────────────────

/** The main mc-typescript client object. Import and use in your scripts. */
export const mc = {
  get player() { return new PlayerAPI(getWsClient()); },
  get move()   { return new MovementAPI(getWsClient()); },
  get interact() { return new InteractAPI(getWsClient()); },
  get inv()    { return new InventoryAPI(getWsClient()); },
  get chest()  { return new ContainerAPI(getWsClient()); },
  get world()  { return new WorldAPI(getWsClient()); },
  get chat()   { return new ChatAPI(getWsClient(), getEventBus()); },

  /**
   * Register an event listener.
   * @example mc.on('hotkey', (key) => { ... })
   */
  on<K extends McEvent>(
    event: K,
    listener: (...args: EventPayloads[K]) => void | Promise<void>
  ): void {
    new EventsAPI(getEventBus()).on(event, listener);
  },

  /**
   * Remove an event listener.
   */
  off<K extends McEvent>(
    event: K,
    listener: (...args: EventPayloads[K]) => void | Promise<void>
  ): void {
    new EventsAPI(getEventBus()).off(event, listener);
  },

  /**
   * Connect to the Forge mod's WebSocket server.
   * This is called automatically by the ScriptHost; you rarely need to call it manually.
   */
  async connect(url?: string): Promise<void> {
    if (url) {
      _wsClient = new WSClient(url);
    }
    await getWsClient().connect();
    // Ensure the event bus is wired up
    getEventBus();
  },

  /** Disconnect from the Forge mod */
  disconnect(): void {
    getWsClient().disconnect();
  },

  /** Whether the library is currently connected to the Forge mod */
  isConnected(): boolean {
    return getWsClient().isConnected();
  },

  /**
   * Send a one-way notification to the Forge mod (fire-and-forget).
   * Used by the ScriptHost to push status updates back to the mod.
   *
   * @param action  Action name, e.g. "scriptHost.scriptStatus"
   * @param params  Payload
   */
  sendAction(action: string, params: Record<string, unknown> = {}): void {
    if (!getWsClient().isConnected()) return;
    getWsClient().request(action, params).catch((err: Error) => {
      console.warn('[mc-typescript] sendAction failed:', err.message);
    });
  },
};

// ──────────────────────────────────────────────────────────────────────────────
// Re-export types so user scripts can import them from "mc-typescript"
// ──────────────────────────────────────────────────────────────────────────────

export type { Vec3, Item, Block, Entity, RaycastResult, BlockFace, PlayerStatus } from './types';
export type { McEvent, EventPayloads } from './api/events';
export { WSClient } from './client/WSClient';
export { EventBus } from './client/EventBus';
