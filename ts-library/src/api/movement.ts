import { WSClient } from '../client/WSClient';
import type { Vec3 } from '../types';

/** Movement / locomotion API (mc.move.*) */
export class MovementAPI {
  constructor(private readonly ws: WSClient) {}

  /** Move forward for the given number of ticks */
  async forward(ticks: number): Promise<void> {
    await this.ws.request('move.forward', { ticks });
  }

  /** Move backward for the given number of ticks */
  async back(ticks: number): Promise<void> {
    await this.ws.request('move.back', { ticks });
  }

  /** Strafe left for the given number of ticks */
  async left(ticks: number): Promise<void> {
    await this.ws.request('move.left', { ticks });
  }

  /** Strafe right for the given number of ticks */
  async right(ticks: number): Promise<void> {
    await this.ws.request('move.right', { ticks });
  }

  /** Perform a single jump */
  async jump(): Promise<void> {
    await this.ws.request('move.jump');
  }

  /** Enable or disable sneaking */
  async sneak(on: boolean): Promise<void> {
    await this.ws.request('move.sneak', { on });
  }

  /** Enable or disable sprinting */
  async sprint(on: boolean): Promise<void> {
    await this.ws.request('move.sprint', { on });
  }

  /** Look at the given world position */
  async lookAt(x: number, y: number, z: number): Promise<void> {
    await this.ws.request('move.lookAt', { x, y, z });
  }

  /**
   * Set the player's rotation directly.
   * @param yaw   Horizontal rotation in degrees (-180 to 180)
   * @param pitch Vertical rotation in degrees (-90 to 90)
   */
  async setLook(yaw: number, pitch: number): Promise<void> {
    await this.ws.request('move.setLook', { yaw, pitch });
  }

  /**
   * Walk to the given world coordinates using simple pathfinding.
   * Resolves when the player arrives within ~1 block of the target.
   */
  async walkTo(x: number, y: number, z: number): Promise<void> {
    await this.ws.request('move.walkTo', { x, y, z });
  }

  /** Stop all movement inputs */
  async stop(): Promise<void> {
    await this.ws.request('move.stop');
  }

  /** Look at a Vec3 object */
  async lookAtVec(pos: Vec3): Promise<void> {
    await this.lookAt(pos.x, pos.y, pos.z);
  }
}
