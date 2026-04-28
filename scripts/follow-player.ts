/**
 * follow-player.ts – Follow a target player around the map.
 *
 * Usage: Set TARGET_NAME to the player you want to follow.
 * Activate with Numpad 2 (or change the hotkey below).
 */
import { mc } from 'mc-typescript'

const TARGET_NAME = 'FreundXY' // Change this to the player name you want to follow
let following = false

mc.on('hotkey', async (key) => {
  if (key !== 'NUMPAD_2') return
  following = !following
  await mc.chat.send(following ? `Folge jetzt ${TARGET_NAME} 👟` : 'Aufgehört zu folgen.')
})

mc.on('tick', async () => {
  if (!following) return

  const entities = await mc.world.entitiesNear(32)
  const target = entities.find(e => e.name === TARGET_NAME)

  if (!target) return

  const me = await mc.player.pos()
  const dist = Math.sqrt(
    Math.pow(target.pos.x - me.x, 2) +
    Math.pow(target.pos.z - me.z, 2)
  )

  if (dist > 3) {
    await mc.move.lookAt(target.pos.x, target.pos.y, target.pos.z)
    await mc.move.forward(2)
  }
})

console.log(`[follow-player] Active – press Numpad 2 to follow ${TARGET_NAME}`)
