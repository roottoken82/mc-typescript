import { WSClient } from '../client/WSClient';
import type { Vec3, PlayerStatus } from '../types';

/** Player information API (mc.player.*) */
export class PlayerAPI {
  constructor(private readonly ws: WSClient) {}

  /** Get the player's current world position */
  async pos(): Promise<Vec3> {
    const result = await this.ws.request('player.pos') as Vec3;
    return result;
  }

  /** Get the player's current health (0–20) */
  async health(): Promise<number> {
    const result = await this.ws.request('player.health') as { health: number };
    return result.health;
  }

  /** Get the player's current hunger level (0–20) */
  async hunger(): Promise<number> {
    const result = await this.ws.request('player.hunger') as { hunger: number };
    return result.hunger;
  }

  /** Get the player's current yaw (horizontal rotation, degrees) */
  async yaw(): Promise<number> {
    const result = await this.ws.request('player.yaw') as { yaw: number };
    return result.yaw;
  }

  /** Get the player's current pitch (vertical rotation, degrees) */
  async pitch(): Promise<number> {
    const result = await this.ws.request('player.pitch') as { pitch: number };
    return result.pitch;
  }

  /** Get a full status snapshot of the player */
  async status(): Promise<PlayerStatus> {
    const result = await this.ws.request('player.status') as PlayerStatus;
    return result;
  }

  /** Get the player's game mode ("survival", "creative", "adventure", "spectator") */
  async gameMode(): Promise<string> {
    const result = await this.ws.request('player.gameMode') as { gameMode: string };
    return result.gameMode;
  }

  /** Get the current dimension the player is in */
  async dimension(): Promise<string> {
    const result = await this.ws.request('player.dimension') as { dimension: string };
    return result.dimension;
  }
}
