/**
 * auto-respond.ts – Automatically reply to chat keywords.
 *
 * Listens for specific keywords in chat and responds automatically.
 */
import { mc } from 'mc-typescript'

const RESPONSES: Record<string, string> = {
  'marco':  'Ja, ich bins 👋',
  'help':   'Brauchst du Hilfe? Zu schlecht 😈',
  'hallo':  'Hallo! 😄',
  'hello':  'Hello! 😄',
  'creeper':'Aua! 💥',
}

mc.on('chat', async (user, msg) => {
  const lowerMsg = msg.toLowerCase()

  for (const [keyword, response] of Object.entries(RESPONSES)) {
    if (lowerMsg.includes(keyword)) {
      // Don't respond to our own messages
      const status = await mc.player.status()
      // Small delay to avoid chat spam
      await new Promise(resolve => setTimeout(resolve, 500))
      await mc.chat.send(`@${user} ${response}`)
      break
    }
  }
})

console.log('[auto-respond] Active – watching chat for keywords:', Object.keys(RESPONSES).join(', '))
