import { WSClient } from '../client/WSClient';
import { EventBus } from '../client/EventBus';

/** Chat API (mc.chat.*) */
export class ChatAPI {
  constructor(
    private readonly ws: WSClient,
    private readonly bus: EventBus
  ) {}

  /**
   * Send a regular chat message as the player.
   * @param message Text to send (max 256 chars)
   */
  async send(message: string): Promise<void> {
    await this.ws.request('chat.send', { message });
  }

  /**
   * Execute a Minecraft command without the leading slash.
   * @example mc.chat.command('gamemode creative')
   */
  async command(cmd: string): Promise<void> {
    await this.ws.request('chat.command', { cmd });
  }

  /**
   * Register a handler that fires whenever another player sends a chat message.
   * @param handler Called with (username, message)
   */
  onMessage(handler: (user: string, msg: string) => void | Promise<void>): void {
    this.bus.on('chat', handler);
  }
}
