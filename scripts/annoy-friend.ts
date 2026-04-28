/**
 * annoy-friend.ts – Push a friend when they get too close 😈
 *
 * When the target player comes within TRIGGER_DISTANCE blocks,
 * attack them (which causes a knockback effect).
 */
import { mc } from 'mc-typescript'

const TARGET_NAME = 'FreundXY'  // Change to your friend's name
const TRIGGER_DISTANCE = 3       // Blocks
const COOLDOWN_MS = 2000         // Minimum ms between pushes

let lastPushTime = 0

mc.on('tick', async () => {
  const now = Date.now()
  if (now - lastPushTime < COOLDOWN_MS) return

  const entities = await mc.world.entitiesNear(TRIGGER_DISTANCE + 1)
  const target = entities.find(e => e.name === TARGET_NAME)

  if (!target) return

  const me = await mc.player.pos()
  const dist = Math.sqrt(
    Math.pow(target.pos.x - me.x, 2) +
    Math.pow(target.pos.z - me.z, 2)
  )

  if (dist <= TRIGGER_DISTANCE) {
    // Look at and attack (knockback) the friend
    await mc.move.lookAt(target.pos.x, target.pos.y, target.pos.z)
    await mc.interact.attackEntity(target.id)
    lastPushTime = now
    console.log(`[annoy-friend] Pushed ${TARGET_NAME}! 😈`)
  }
})

console.log(`[annoy-friend] Active – will push ${TARGET_NAME} when closer than ${TRIGGER_DISTANCE} blocks`)
