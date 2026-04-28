/**
 * chest-thief.ts – Steal everything from the nearest chest on Numpad 1.
 *
 * This is the primary example script mentioned in the project requirements.
 */
import { mc } from 'mc-typescript'

mc.on('hotkey', async (key) => {
  if (key !== 'NUMPAD_1') return

  const me = await mc.player.pos()
  const chestPos = await mc.world.findBlock('minecraft:chest', 5)

  if (!chestPos) {
    await mc.chat.send('Keine Kiste in der Nähe :(')
    return
  }

  // Look at the chest and open it
  await mc.move.lookAt(chestPos.x, chestPos.y, chestPos.z)
  await mc.chest.openAt(chestPos.x, chestPos.y, chestPos.z)

  // Wait a tick for the container to open
  await new Promise(resolve => setTimeout(resolve, 100))

  // Shift-click every item into our inventory
  const slots = await mc.chest.slots()
  for (const item of slots) {
    if (item) {
      await mc.chest.quickMove(item.slot)
    }
  }

  await mc.chest.close()
  await mc.chat.send('💀 geklaut')
})
