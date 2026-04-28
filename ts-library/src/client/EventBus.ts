type EventListener<T extends unknown[]> = (...args: T) => void | Promise<void>;

/** Simple typed event bus */
export class EventBus {
  private listeners: Map<string, EventListener<unknown[]>[]> = new Map();

  /** Register a listener for the given event */
  on<T extends unknown[]>(event: string, listener: EventListener<T>): void {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, []);
    }
    this.listeners.get(event)!.push(listener as EventListener<unknown[]>);
  }

  /** Remove a previously registered listener */
  off<T extends unknown[]>(event: string, listener: EventListener<T>): void {
    const list = this.listeners.get(event);
    if (!list) return;
    const idx = list.indexOf(listener as EventListener<unknown[]>);
    if (idx !== -1) list.splice(idx, 1);
  }

  /** Emit an event, calling all registered listeners */
  emit(event: string, ...args: unknown[]): void {
    const list = this.listeners.get(event);
    if (!list) return;
    for (const listener of [...list]) {
      try {
        const result = listener(...args);
        if (result instanceof Promise) {
          result.catch((err) => console.error(`[EventBus] Unhandled error in "${event}" listener:`, err));
        }
      } catch (err) {
        console.error(`[EventBus] Error in "${event}" listener:`, err);
      }
    }
  }

  /** Remove all listeners for an event (or all events if no name given) */
  removeAll(event?: string): void {
    if (event) {
      this.listeners.delete(event);
    } else {
      this.listeners.clear();
    }
  }
}
