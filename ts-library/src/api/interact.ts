import { WSClient } from '../client/WSClient';
import type { BlockFace } from '../types';

/** Interaction API – mouse clicks & entity interactions (mc.interact.*) */
export class InteractAPI {
  constructor(private readonly ws: WSClient) {}

  /** Perform a single left-click (attack) in the direction the player is looking */
  async attack(): Promise<void> {
    await this.ws.request('interact.attack');
  }

  /** Perform a single right-click (use) in the direction the player is looking */
  async use(): Promise<void> {
    await this.ws.request('interact.use');
  }

  /** Start holding down the left-click (block breaking) */
  async startBreaking(): Promise<void> {
    await this.ws.request('interact.startBreaking');
  }

  /** Stop holding down the left-click */
  async stopBreaking(): Promise<void> {
    await this.ws.request('interact.stopBreaking');
  }

  /**
   * Place a block at the given world position on the specified face.
   * The active hotbar item is used.
   */
  async placeBlockAt(x: number, y: number, z: number, face: BlockFace = 'up'): Promise<void> {
    await this.ws.request('interact.placeBlockAt', { x, y, z, face });
  }

  /** Use the active item on the given entity */
  async useItemOnEntity(entityId: number): Promise<void> {
    await this.ws.request('interact.useItemOnEntity', { entityId });
  }

  /** Attack (left-click) the given entity */
  async attackEntity(entityId: number): Promise<void> {
    await this.ws.request('interact.attackEntity', { entityId });
  }
}
