import WebSocket from 'ws';

/** Pending request waiting for a response from the mod */
interface PendingRequest {
  resolve: (value: unknown) => void;
  reject: (reason?: unknown) => void;
  timeout: NodeJS.Timeout;
}

/** Low-level WebSocket client that communicates with the Forge mod */
export class WSClient {
  private ws: WebSocket | null = null;
  private pendingRequests: Map<string, PendingRequest> = new Map();
  private messageHandlers: Array<(msg: Record<string, unknown>) => void> = [];
  private reconnectTimer: NodeJS.Timeout | null = null;
  private connected = false;
  private requestTimeoutMs: number;

  constructor(
    private readonly url: string = 'ws://localhost:8765',
    requestTimeoutMs = 10_000
  ) {
    this.requestTimeoutMs = requestTimeoutMs;
  }

  /** Connect (or reconnect) to the WebSocket server */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.connected) {
        resolve();
        return;
      }

      console.log(`[WSClient] Connecting to ${this.url} …`);
      this.ws = new WebSocket(this.url);

      this.ws.on('open', () => {
        this.connected = true;
        console.log('[WSClient] Connected to Forge mod');
        resolve();
      });

      this.ws.on('message', (data) => {
        try {
          const msg = JSON.parse(data.toString()) as Record<string, unknown>;
          this.handleMessage(msg);
        } catch (e) {
          console.error('[WSClient] Failed to parse message:', data.toString());
        }
      });

      this.ws.on('close', () => {
        this.connected = false;
        console.log('[WSClient] Disconnected – reconnecting in 3 s …');
        this.scheduleReconnect();
      });

      this.ws.on('error', (err) => {
        if (!this.connected) {
          reject(err);
        } else {
          console.error('[WSClient] WebSocket error:', err.message);
        }
      });
    });
  }

  /** Disconnect from the WebSocket server */
  disconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    this.ws?.close();
    this.connected = false;
  }

  /** Whether the client is currently connected */
  isConnected(): boolean {
    return this.connected;
  }

  /**
   * Send an action request and wait for the response.
   * @param action  Action name, e.g. "chat.send"
   * @param params  Action parameters
   * @returns       Result from the mod
   */
  request(action: string, params: Record<string, unknown> = {}): Promise<unknown> {
    return new Promise((resolve, reject) => {
      if (!this.ws || !this.connected) {
        reject(new Error('[WSClient] Not connected to Forge mod'));
        return;
      }

      const id = this.generateId();
      const timeout = setTimeout(() => {
        this.pendingRequests.delete(id);
        reject(new Error(`[WSClient] Request "${action}" timed out after ${this.requestTimeoutMs} ms`));
      }, this.requestTimeoutMs);

      this.pendingRequests.set(id, { resolve, reject, timeout });

      const payload = JSON.stringify({ id, action, params });
      this.ws.send(payload);
    });
  }

  /** Register a handler for incoming messages (events, etc.) */
  onMessage(handler: (msg: Record<string, unknown>) => void): void {
    this.messageHandlers.push(handler);
  }

  // ──────────────────────────────────────────────────────────────
  // Private helpers
  // ──────────────────────────────────────────────────────────────

  private handleMessage(msg: Record<string, unknown>): void {
    // Response to a pending request
    if (typeof msg['id'] === 'string' && this.pendingRequests.has(msg['id'])) {
      const pending = this.pendingRequests.get(msg['id'])!;
      clearTimeout(pending.timeout);
      this.pendingRequests.delete(msg['id'] as string);

      if (msg['ok'] === true) {
        pending.resolve(msg['result']);
      } else {
        pending.reject(new Error(String(msg['error'] ?? 'Unknown error')));
      }
      return;
    }

    // Event broadcast from the mod
    for (const handler of this.messageHandlers) {
      handler(msg);
    }
  }

  private scheduleReconnect(): void {
    this.reconnectTimer = setTimeout(() => {
      this.connect().catch(() => {
        // will retry via the 'close' handler
      });
    }, 3_000);
  }

  private generateId(): string {
    return Math.random().toString(36).slice(2, 10);
  }
}
