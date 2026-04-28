import { WSClient } from '../client/WSClient';
import type { Item } from '../types';

/** Inventory management API (mc.inv.*) */
export class InventoryAPI {
  constructor(private readonly ws: WSClient) {}

  /** List all items in the player's inventory */
  async list(): Promise<Item[]> {
    const result = await this.ws.request('inv.list') as { items: Item[] };
    return result.items;
  }

  /**
   * Select a hotbar slot.
   * @param slot 0–8
   */
  async selectHotbar(slot: number): Promise<void> {
    if (slot < 0 || slot > 8) throw new RangeError(`Hotbar slot must be 0–8, got ${slot}`);
    await this.ws.request('inv.selectHotbar', { slot });
  }

  /**
   * Swap two inventory slots.
   * @param slotA Source slot index
   * @param slotB Target slot index
   */
  async swap(slotA: number, slotB: number): Promise<void> {
    await this.ws.request('inv.swap', { slotA, slotB });
  }

  /**
   * Drop an item from a slot.
   * @param slot  Inventory slot index
   * @param all   If true, drop the entire stack
   */
  async drop(slot: number, all = false): Promise<void> {
    await this.ws.request('inv.drop', { slot, all });
  }

  /**
   * Find the first slot that contains an item with the given name.
   * @param name Internal item name, e.g. "minecraft:diamond"
   * @returns Slot index, or -1 if not found
   */
  async findItem(name: string): Promise<number> {
    const result = await this.ws.request('inv.findItem', { name }) as { slot: number };
    return result.slot;
  }
}
