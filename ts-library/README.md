# mc-typescript – TypeScript Library

NPM package that provides the TypeScript API for controlling your Minecraft client via the Forge mod bridge.

## Installation

```bash
npm install
```

## Build

```bash
npm run build
# → dist/
```

## Development (hot-reload)

```bash
npm run dev
```

## Usage

```typescript
import { mc } from 'mc-typescript'

// The library auto-connects to ws://localhost:8765
// Override with MC_WS_URL environment variable

const pos = await mc.player.pos()
console.log('Player at', pos)

mc.on('hotkey', async (key) => {
  console.log('Hotkey pressed:', key)
})
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `MC_WS_URL` | `ws://localhost:8765` | WebSocket URL of the Forge mod server |

## API

See [`../docs/ACTIONS.md`](../docs/ACTIONS.md) for the full API reference.
