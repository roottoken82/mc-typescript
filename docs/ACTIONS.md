# Actions & Events Reference – mc-typescript

Complete reference for all TypeScript API calls and game events.

---

## Player API (`mc.player.*`)

| Method | Returns | Description |
|--------|---------|-------------|
| `mc.player.pos()` | `Vec3` | Current world position |
| `mc.player.health()` | `number` | Health (0–20) |
| `mc.player.hunger()` | `number` | Food level (0–20) |
| `mc.player.yaw()` | `number` | Horizontal rotation in degrees |
| `mc.player.pitch()` | `number` | Vertical rotation in degrees |
| `mc.player.status()` | `PlayerStatus` | Full snapshot (pos + rotation + health + hunger + gameMode + dimension) |
| `mc.player.gameMode()` | `string` | "survival" \| "creative" \| "adventure" \| "spectator" |
| `mc.player.dimension()` | `string` | e.g. "minecraft:overworld" |

---

## Movement API (`mc.move.*`)

All methods return `Promise<void>`.

| Method | Parameters | Description |
|--------|-----------|-------------|
| `mc.move.forward(ticks)` | `ticks: number` | Hold W for N ticks |
| `mc.move.back(ticks)` | `ticks: number` | Hold S for N ticks |
| `mc.move.left(ticks)` | `ticks: number` | Hold A for N ticks |
| `mc.move.right(ticks)` | `ticks: number` | Hold D for N ticks |
| `mc.move.jump()` | – | Single jump |
| `mc.move.sneak(on)` | `on: boolean` | Toggle sneak |
| `mc.move.sprint(on)` | `on: boolean` | Toggle sprint |
| `mc.move.lookAt(x, y, z)` | `x, y, z: number` | Look at world position |
| `mc.move.setLook(yaw, pitch)` | `yaw, pitch: number` | Set rotation directly |
| `mc.move.walkTo(x, y, z)` | `x, y, z: number` | Walk to position (greedy) |
| `mc.move.stop()` | – | Stop all movement |
| `mc.move.lookAtVec(pos)` | `pos: Vec3` | Look at Vec3 object |

---

## Interaction API (`mc.interact.*`)

All methods return `Promise<void>`.

| Method | Parameters | Description |
|--------|-----------|-------------|
| `mc.interact.attack()` | – | Single left-click |
| `mc.interact.use()` | – | Single right-click |
| `mc.interact.startBreaking()` | – | Hold left-click |
| `mc.interact.stopBreaking()` | – | Release left-click |
| `mc.interact.placeBlockAt(x,y,z,face)` | `face?: BlockFace` | Place block on face |
| `mc.interact.useItemOnEntity(id)` | `entityId: number` | Right-click entity |
| `mc.interact.attackEntity(id)` | `entityId: number` | Left-click entity |

`BlockFace` values: `"up"` | `"down"` | `"north"` | `"south"` | `"east"` | `"west"`

---

## Inventory API (`mc.inv.*`)

| Method | Returns | Description |
|--------|---------|-------------|
| `mc.inv.list()` | `Item[]` | All items in player inventory |
| `mc.inv.selectHotbar(slot)` | `void` | Select hotbar slot 0–8 |
| `mc.inv.swap(slotA, slotB)` | `void` | Swap two slots |
| `mc.inv.drop(slot, all?)` | `void` | Drop item(s) from slot |
| `mc.inv.findItem(name)` | `number` | First slot with item, or -1 |

---

## Container API (`mc.chest.*`)

Used for chests, barrels, shulker boxes, furnaces, hoppers, etc.

| Method | Returns | Description |
|--------|---------|-------------|
| `mc.chest.openAt(x, y, z)` | `void` | Open container at position |
| `mc.chest.slots()` | `(Item \| null)[]` | All container slots |
| `mc.chest.take(slot, count?)` | `void` | Take item(s) from slot |
| `mc.chest.put(invSlot, containerSlot?, count?)` | `void` | Put item from inventory into container |
| `mc.chest.quickMove(slot)` | `void` | Shift-click slot |
| `mc.chest.close()` | `void` | Close container |

---

## World API (`mc.world.*`)

| Method | Returns | Description |
|--------|---------|-------------|
| `mc.world.getBlock(x, y, z)` | `Block \| null` | Block info at position |
| `mc.world.raycast(maxDist?)` | `RaycastResult` | Ray from player eyes |
| `mc.world.entitiesNear(radius)` | `Entity[]` | Entities within radius |
| `mc.world.findBlock(name, radius?)` | `Vec3 \| null` | Find nearest block by name |
| `mc.world.spawnPoint()` | `Vec3` | Player's spawn point |

---

## Chat API (`mc.chat.*`)

| Method | Returns | Description |
|--------|---------|-------------|
| `mc.chat.send(msg)` | `void` | Send chat message |
| `mc.chat.command(cmd)` | `void` | Run command (no leading `/`) |
| `mc.chat.onMessage(handler)` | `void` | Register chat listener |

---

## Events (`mc.on`)

```typescript
mc.on(event: McEvent, listener: (...args) => void | Promise<void>): void
mc.off(event: McEvent, listener): void
```

| Event | Listener Args | Description |
|-------|--------------|-------------|
| `'chat'` | `(user: string, msg: string)` | Player chat received |
| `'hotkey'` | `(key: string)` | Hotkey pressed (e.g. `"NUMPAD_1"`) |
| `'tick'` | `()` | Every game tick (~20/s) |
| `'damage'` | `(source: string, amount: number)` | Player took damage |
| `'playerJoin'` | `(name: string)` | Player joined server |
| `'playerLeave'` | `(name: string)` | Player left server |
| `'death'` | `()` | Local player died |

### Hotkey Names

| Key | String |
|-----|--------|
| K (Menu) | `"MENU"` (triggers GUI, not broadcast) |
| Numpad 1 | `"NUMPAD_1"` |
| Numpad 2 | `"NUMPAD_2"` |
| … | … |
| Numpad 9 | `"NUMPAD_9"` |

---

## Types

```typescript
interface Vec3 {
  x: number; y: number; z: number;
}

interface Item {
  name: string;        // "minecraft:diamond"
  displayName: string;
  count: number;
  slot: number;
  nbt?: string;
}

interface Block {
  name: string;
  state: Record<string, string>;
  pos: Vec3;
}

interface Entity {
  id: number;
  type: string;        // "minecraft:player"
  name?: string;
  pos: Vec3;
  health?: number;
}

interface RaycastResult {
  hit: boolean;
  block?: Block;
  entity?: Entity;
  pos?: Vec3;
}

interface PlayerStatus {
  pos: Vec3;
  yaw: number;
  pitch: number;
  health: number;
  hunger: number;
  gameMode: string;
  dimension: string;
}

type BlockFace = 'up' | 'down' | 'north' | 'south' | 'east' | 'west';
```

---

## Example: chest-thief

```typescript
import { mc } from 'mc-typescript'

mc.on('hotkey', async (key) => {
  if (key !== 'NUMPAD_1') return

  const chestPos = await mc.world.findBlock('minecraft:chest', 5)
  if (!chestPos) return mc.chat.send('Keine Kiste in der Nähe :(')

  await mc.move.lookAt(chestPos.x, chestPos.y, chestPos.z)
  await mc.chest.openAt(chestPos.x, chestPos.y, chestPos.z)

  for (const item of await mc.chest.slots()) {
    if (item) await mc.chest.quickMove(item.slot)
  }

  await mc.chest.close()
  await mc.chat.send('💀 geklaut')
})
```
