import { WSClient } from '../client/WSClient';
import type { Item } from '../types';

/** Container / chest API (mc.chest.*) */
export class ContainerAPI {
  constructor(private readonly ws: WSClient) {}

  /**
   * Open the container block (chest, barrel, furnace, etc.) at the given position.
   * The player must be close enough to reach it.
   */
  async openAt(x: number, y: number, z: number): Promise<void> {
    await this.ws.request('chest.openAt', { x, y, z });
  }

  /**
   * List all item stacks in the currently open container.
   * Slots without items are represented as null in the array.
   */
  async slots(): Promise<(Item | null)[]> {
    const result = await this.ws.request('chest.slots') as { slots: (Item | null)[] };
    return result.slots;
  }

  /**
   * Take items from a container slot into the player's inventory.
   * @param slot  Container slot index
   * @param count Number of items to take (defaults to full stack)
   */
  async take(slot: number, count?: number): Promise<void> {
    await this.ws.request('chest.take', { slot, count: count ?? -1 });
  }

  /**
   * Put items from the player's inventory into a container slot.
   * @param invSlot        Player inventory slot
   * @param containerSlot  Target container slot (optional – auto-fills if omitted)
   * @param count          Number of items to put (defaults to full stack)
   */
  async put(invSlot: number, containerSlot?: number, count?: number): Promise<void> {
    await this.ws.request('chest.put', {
      invSlot,
      containerSlot: containerSlot ?? -1,
      count: count ?? -1,
    });
  }

  /**
   * Shift-click a container slot (quick-move between container and inventory).
   * @param slot Container slot index
   */
  async quickMove(slot: number): Promise<void> {
    await this.ws.request('chest.quickMove', { slot });
  }

  /** Close the currently open container */
  async close(): Promise<void> {
    await this.ws.request('chest.close');
  }
}
