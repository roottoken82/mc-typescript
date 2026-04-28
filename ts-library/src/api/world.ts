import { WSClient } from '../client/WSClient';
import type { Block, Entity, RaycastResult, Vec3 } from '../types';

/** World information API (mc.world.*) */
export class WorldAPI {
  constructor(private readonly ws: WSClient) {}

  /**
   * Get the block at the given world position.
   * @returns Block info, or null if outside the loaded chunks
   */
  async getBlock(x: number, y: number, z: number): Promise<Block | null> {
    const result = await this.ws.request('world.getBlock', { x, y, z }) as { block: Block | null };
    return result.block;
  }

  /**
   * Perform a ray-cast from the player's eyes in the direction they are looking.
   * @param maxDist Maximum ray distance in blocks (default: 4.5)
   */
  async raycast(maxDist = 4.5): Promise<RaycastResult> {
    const result = await this.ws.request('world.raycast', { maxDist }) as RaycastResult;
    return result;
  }

  /**
   * Get all entities within the given radius around the player.
   * @param radius Search radius in blocks
   */
  async entitiesNear(radius: number): Promise<Entity[]> {
    const result = await this.ws.request('world.entitiesNear', { radius }) as { entities: Entity[] };
    return result.entities;
  }

  /**
   * Find the nearest block with the given name within the given radius.
   * @param name   Internal block name, e.g. "minecraft:chest"
   * @param radius Search radius in blocks (default: 16)
   * @returns Block position, or null if not found
   */
  async findBlock(name: string, radius = 16): Promise<Vec3 | null> {
    const result = await this.ws.request('world.findBlock', { name, radius }) as { pos: Vec3 | null };
    return result.pos;
  }

  /** Get the player's current spawn point */
  async spawnPoint(): Promise<Vec3> {
    const result = await this.ws.request('world.spawnPoint') as Vec3;
    return result;
  }
}
