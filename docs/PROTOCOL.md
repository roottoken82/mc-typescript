# WebSocket Protocol – mc-typescript

The Forge mod and the TypeScript runtime communicate over a **local WebSocket connection** on port `8765` (configurable).

Messages are JSON objects. There are two directions:

| Direction | Purpose |
|-----------|---------|
| **TS → Mod** (Request) | Execute a game action |
| **Mod → TS** (Response) | Confirm the action result or report an error |
| **Mod → TS** (Event) | Notify the TS runtime of a game event |

---

## Request (TS → Mod)

```json
{
  "id":     "abc123",
  "action": "chat.send",
  "params": {
    "message": "Hello world!"
  }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | `string` | Unique request ID used to match the response |
| `action` | `string` | Dot-separated action name (see Actions below) |
| `params` | `object` | Action-specific parameters (may be empty `{}`) |

---

## Response (Mod → TS)

### Success

```json
{
  "id":     "abc123",
  "ok":     true,
  "result": { }
}
```

### Error

```json
{
  "id":    "abc123",
  "ok":    false,
  "error": "Player not available"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | `string` | Matching request ID |
| `ok` | `boolean` | `true` on success, `false` on error |
| `result` | `object` | Return value (action-specific, may be empty) |
| `error` | `string` | Error message (only present when `ok` is false) |

---

## Event (Mod → TS)

Events have **no `id`** field – they are broadcast to all connected clients.

```json
{
  "event": "hotkey",
  "data":  { "key": "NUMPAD_1" }
}
```

| Field | Type | Description |
|-------|------|-------------|
| `event` | `string` | Event name |
| `data` | `object` | Event-specific payload |

---

## All Actions

### Player

| Action | Params | Result |
|--------|--------|--------|
| `player.pos` | – | `{ x, y, z }` |
| `player.health` | – | `{ health: number }` |
| `player.hunger` | – | `{ hunger: number }` |
| `player.yaw` | – | `{ yaw: number }` |
| `player.pitch` | – | `{ pitch: number }` |
| `player.status` | – | `{ x, y, z, yaw, pitch, health, hunger, gameMode, dimension }` |
| `player.gameMode` | – | `{ gameMode: string }` |
| `player.dimension` | – | `{ dimension: string }` |

### Chat

| Action | Params | Result |
|--------|--------|--------|
| `chat.send` | `{ message: string }` | `{}` |
| `chat.command` | `{ cmd: string }` | `{}` |

### Movement

| Action | Params | Result |
|--------|--------|--------|
| `move.forward` | `{ ticks: number }` | `{}` |
| `move.back` | `{ ticks: number }` | `{}` |
| `move.left` | `{ ticks: number }` | `{}` |
| `move.right` | `{ ticks: number }` | `{}` |
| `move.jump` | – | `{}` |
| `move.sneak` | `{ on: boolean }` | `{}` |
| `move.sprint` | `{ on: boolean }` | `{}` |
| `move.lookAt` | `{ x, y, z: number }` | `{}` |
| `move.setLook` | `{ yaw, pitch: number }` | `{}` |
| `move.walkTo` | `{ x, y, z: number }` | `{}` |
| `move.stop` | – | `{}` |

### Interaction

| Action | Params | Result |
|--------|--------|--------|
| `interact.attack` | – | `{}` |
| `interact.use` | – | `{}` |
| `interact.startBreaking` | – | `{}` |
| `interact.stopBreaking` | – | `{}` |
| `interact.placeBlockAt` | `{ x, y, z: number, face: string }` | `{}` |
| `interact.useItemOnEntity` | `{ entityId: number }` | `{}` |
| `interact.attackEntity` | `{ entityId: number }` | `{}` |

### Inventory

| Action | Params | Result |
|--------|--------|--------|
| `inv.list` | – | `{ items: Item[] }` |
| `inv.selectHotbar` | `{ slot: 0–8 }` | `{}` |
| `inv.swap` | `{ slotA, slotB: number }` | `{}` |
| `inv.drop` | `{ slot: number, all?: boolean }` | `{}` |
| `inv.findItem` | `{ name: string }` | `{ slot: number }` (-1 = not found) |

### Container

| Action | Params | Result |
|--------|--------|--------|
| `chest.openAt` | `{ x, y, z: number }` | `{}` |
| `chest.slots` | – | `{ slots: (Item \| null)[] }` |
| `chest.take` | `{ slot: number, count?: number }` | `{}` |
| `chest.put` | `{ invSlot: number, containerSlot?: number, count?: number }` | `{}` |
| `chest.quickMove` | `{ slot: number }` | `{}` |
| `chest.close` | – | `{}` |

### World

| Action | Params | Result |
|--------|--------|--------|
| `world.getBlock` | `{ x, y, z: number }` | `{ block: Block \| null }` |
| `world.raycast` | `{ maxDist?: number }` | `RaycastResult` |
| `world.entitiesNear` | `{ radius: number }` | `{ entities: Entity[] }` |
| `world.findBlock` | `{ name: string, radius?: number }` | `{ pos: Vec3 \| null }` |
| `world.spawnPoint` | – | `{ x, y, z }` |

### ScriptHost

| Action | Params | Result |
|--------|--------|--------|
| `scriptHost.listScripts` | – | `{ scripts: string[] }` |

---

## All Events

| Event | Data | Description |
|-------|------|-------------|
| `chat` | `{ user: string, message: string }` | Chat message received |
| `hotkey` | `{ key: string }` | Configured hotkey pressed |
| `tick` | `{}` | Every game tick (20/s) |
| `damage` | `{ source: string, amount: number }` | Player took damage |
| `playerJoin` | `{ name: string }` | Player joined the server |
| `playerLeave` | `{ name: string }` | Player left the server |
| `death` | `{}` | Local player died |
| `runScript` | `{ script: string }` | Mod requests TS runtime to run a script |
| `stopScripts` | `{}` | Mod requests TS runtime to stop all scripts |
| `reloadScripts` | `{}` | Mod requests TS runtime to reload script list |

---

## Type Definitions

### Item

```json
{
  "name": "minecraft:diamond",
  "displayName": "Diamond",
  "count": 3,
  "slot": 7,
  "nbt": "{...}"
}
```

### Block

```json
{
  "name": "minecraft:chest",
  "state": { "facing": "north", "type": "single" },
  "pos": { "x": 10, "y": 64, "z": -5 }
}
```

### Entity

```json
{
  "id": 42,
  "type": "minecraft:player",
  "name": "FreundXY",
  "pos": { "x": 10.5, "y": 64.0, "z": -5.2 },
  "health": 18.5
}
```

### RaycastResult

```json
{
  "hit": true,
  "block": { ... },
  "pos": { "x": 10.5, "y": 64.5, "z": -5.0 }
}
```
