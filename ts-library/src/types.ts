// Shared types used across the mc-typescript library

/** 3D coordinate / vector */
export interface Vec3 {
  x: number;
  y: number;
  z: number;
}

/** An item stack in an inventory slot */
export interface Item {
  /** Internal Minecraft name, e.g. "minecraft:diamond" */
  name: string;
  /** Display name */
  displayName: string;
  /** Stack count */
  count: number;
  /** Inventory slot index */
  slot: number;
  /** Item NBT data (JSON string) */
  nbt?: string;
}

/** A block in the world */
export interface Block {
  /** Internal name, e.g. "minecraft:chest" */
  name: string;
  /** Block-state properties as key→value map */
  state: Record<string, string>;
  /** World position */
  pos: Vec3;
}

/** A living entity (player, mob, etc.) */
export interface Entity {
  /** Runtime entity ID */
  id: number;
  /** Entity type, e.g. "minecraft:player" */
  type: string;
  /** Display / player name (if applicable) */
  name?: string;
  /** World position */
  pos: Vec3;
  /** Remaining health */
  health?: number;
}

/** Result of a ray-cast operation */
export interface RaycastResult {
  /** Whether the ray hit something */
  hit: boolean;
  /** Hit block (if any) */
  block?: Block;
  /** Hit entity (if any) */
  entity?: Entity;
  /** Exact hit position */
  pos?: Vec3;
}

/** Block face for placement operations */
export type BlockFace = 'up' | 'down' | 'north' | 'south' | 'east' | 'west';

/** Player status snapshot */
export interface PlayerStatus {
  pos: Vec3;
  yaw: number;
  pitch: number;
  health: number;
  hunger: number;
  gameMode: string;
  dimension: string;
}
