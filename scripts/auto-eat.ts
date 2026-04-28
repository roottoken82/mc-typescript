/**
 * auto-eat.ts – Automatically eat food when hunger drops below 6.
 *
 * Looks for food in the inventory, selects it, and holds right-click.
 * Works best in survival mode.
 */
import { mc } from 'mc-typescript'

/** Minecraft food items (internal names) */
const FOOD_ITEMS = [
  'minecraft:cooked_beef',
  'minecraft:cooked_porkchop',
  'minecraft:cooked_chicken',
  'minecraft:bread',
  'minecraft:apple',
  'minecraft:golden_apple',
  'minecraft:cooked_mutton',
  'minecraft:cooked_salmon',
  'minecraft:cooked_cod',
  'minecraft:baked_potato',
]

const HUNGER_THRESHOLD = 6
let eating = false

mc.on('tick', async () => {
  if (eating) return

  const hunger = await mc.player.hunger()
  if (hunger >= HUNGER_THRESHOLD) return

  // Find food in inventory
  let foodSlot = -1
  for (const foodItem of FOOD_ITEMS) {
    foodSlot = await mc.inv.findItem(foodItem)
    if (foodSlot !== -1) break
  }

  if (foodSlot === -1) return // No food found

  eating = true
  try {
    const prevSlot = (await mc.inv.list()).find(i => i.slot < 9)?.slot ?? 0
    await mc.inv.selectHotbar(Math.min(foodSlot, 8))
    await mc.interact.use()
    // Hold use for 1.6 seconds (eating animation)
    await new Promise(resolve => setTimeout(resolve, 1600))
  } finally {
    eating = false
  }
})

console.log('[auto-eat] Active – will eat automatically when hunger < ' + HUNGER_THRESHOLD)
