import { EventBus } from '../client/EventBus';

/** All event names supported by mc.on() */
export type McEvent =
  | 'chat'
  | 'hotkey'
  | 'tick'
  | 'damage'
  | 'playerJoin'
  | 'playerLeave'
  | 'death';

/** Event payload types */
export interface EventPayloads {
  /** Fired when another player sends a chat message */
  chat: [user: string, message: string];
  /** Fired when a configured hotkey is pressed */
  hotkey: [key: string];
  /** Fired every game tick (20 times/second) */
  tick: [];
  /** Fired when the player takes damage */
  damage: [source: string, amount: number];
  /** Fired when a player joins the server */
  playerJoin: [name: string];
  /** Fired when a player leaves the server */
  playerLeave: [name: string];
  /** Fired when the player dies */
  death: [];
}

/** Events API – register listeners for game events (mc.on) */
export class EventsAPI {
  constructor(private readonly bus: EventBus) {}

  /**
   * Register an event listener.
   *
   * @example
   * mc.on('hotkey', (key) => { if (key === 'NUMPAD_1') { ... } })
   * mc.on('chat', (user, msg) => { ... })
   * mc.on('tick', () => { ... })
   */
  on<K extends McEvent>(
    event: K,
    listener: (...args: EventPayloads[K]) => void | Promise<void>
  ): void {
    // EventBus is untyped internally; cast to satisfy TypeScript
    this.bus.on(event, listener as (...args: unknown[]) => void | Promise<void>);
  }

  /**
   * Remove a previously registered event listener.
   */
  off<K extends McEvent>(
    event: K,
    listener: (...args: EventPayloads[K]) => void | Promise<void>
  ): void {
    this.bus.off(event, listener as (...args: unknown[]) => void | Promise<void>);
  }
}
