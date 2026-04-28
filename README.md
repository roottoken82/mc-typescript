# mc-typescript

> **TypeScript-Library + Forge-Mod-Bridge** – steuere deinen Minecraft-Client (1.20.1) per TypeScript-Skript, In-Game-Command, Hotkey und GUI.  
> Perfekt für privaten Spaß auf Aternos-Servern 😈 – **alles läuft client-seitig**, keine Server-Mod nötig.

---

## Architektur (Kurzfassung)

```
Minecraft Client (Forge-Mod)  ◄── WebSocket :8765 ──►  Node.js / TypeScript
       "Body"                                                  "Gehirn"
```

Die Forge-Mod startet einen lokalen WebSocket-Server auf Port `8765`.  
Die TS-Library verbindet sich damit und sendet JSON-Befehle, die Mod führt sie im Spiel aus.

---

## 1. Forge-Mod installieren

### Voraussetzungen
- **Minecraft Java Edition** (offizieller Launcher, Microsoft-Account)
- **Forge 47.2.0** für Minecraft **1.20.1** → [Download Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)

### Schritte
1. Forge 1.20.1 installieren (Installer von der Forge-Webseite ausführen)
2. `mc-typescript-1.0.0.jar` (aus `forge-mod/build/libs/`) in den `mods`-Ordner legen:
   - Windows: `%APPDATA%\.minecraft\mods\`
   - macOS/Linux: `~/.minecraft/mods/`
3. Minecraft mit dem Forge-Profil starten

### Mod bauen (optional – wenn du Änderungen machst)
```bash
cd forge-mod
./gradlew build
# → forge-mod/build/libs/mc-typescript-1.0.0.jar
```

---

## 2. Mit Aternos-Server verbinden

Ganz normal! Keine extra Schritte nötig:

1. Minecraft starten → Multiplayer → Server-Adresse eintragen (z. B. `deinserver.aternos.me`)
2. Mit deinem Microsoft-Account einloggen (wie immer)
3. Die Mod läuft **in deinem Client** – der Aternos-Server merkt nichts davon
4. Im Spiel `/mcts enable` eingeben (Sicherheits-Toggle)

> **Hinweis:** Aternos-Server laufen als Vanilla/Paper. Auf dem Server ist **keine Mod nötig**.

---

## 3. TypeScript-Runtime starten

```bash
# Repository klonen / Ordner öffnen
cd ts-library

# Abhängigkeiten installieren
npm install

# Entwicklungsmodus (Hot-Reload für Skripte)
npm run dev

# Oder einmalig starten
npm start
```

Der Runtime verbindet sich automatisch mit `ws://localhost:8765` (der Mod muss laufen).

---

## 4. Eigenes Skript schreiben

Erstelle eine `.ts`-Datei im `scripts/`-Ordner:

```typescript
// scripts/mein-skript.ts
import { mc } from 'mc-typescript'

// Auf Hotkey reagieren
mc.on('hotkey', async (key) => {
  if (key !== 'NUMPAD_1') return
  await mc.chat.send('Hallo Welt! 👋')
})

// Auf Chat-Nachricht reagieren
mc.on('chat', async (user, msg) => {
  if (msg.includes('marco')) {
    await mc.chat.send(`@${user} hast du was gesagt? 😈`)
  }
})
```

Skript starten:
- **In-Game:** `/mcts run mein-skript`
- **Hotkey:** Standard `K` öffnet das Skript-Menü
- **Terminal:** Runtime erkennt neue Dateien automatisch (Hot-Reload)

---

## 5. Verfügbare APIs

```typescript
import { mc } from 'mc-typescript'

// Spieler-Info
const pos    = await mc.player.pos()      // { x, y, z }
const health = await mc.player.health()   // number
const hunger = await mc.player.hunger()   // number

// Bewegung
await mc.move.forward(20)                 // 20 Ticks vorwärts gehen
await mc.move.jump()
await mc.move.sprint(true)
await mc.move.lookAt(x, y, z)
await mc.move.walkTo(x, y, z)            // einfaches Pathfinding

// Interaktion
await mc.interact.attack()               // Linksklick
await mc.interact.use()                  // Rechtsklick
await mc.interact.attackEntity(entityId)

// Inventar
const items = await mc.inv.list()
await mc.inv.selectHotbar(0)             // Slot 0–8
await mc.inv.drop(slot)

// Kisten
await mc.chest.openAt(x, y, z)
const slots = await mc.chest.slots()
await mc.chest.quickMove(slot)           // Shift-Klick
await mc.chest.close()

// Welt
const block   = await mc.world.getBlock(x, y, z)
const nearby  = await mc.world.entitiesNear(10)
const chest   = await mc.world.findBlock('minecraft:chest', 5)

// Chat
await mc.chat.send('Hallo!')
await mc.chat.command('gamemode creative')
```

---

## 6. In-Game-Befehle

| Befehl | Beschreibung |
|--------|-------------|
| `/mcts enable` | Mod aktivieren (Pflicht vor ersten Aktionen) |
| `/mcts disable` | Mod deaktivieren |
| `/mcts run <name>` | Skript starten |
| `/mcts stop` | Laufendes Skript stoppen |
| `/mcts status` | Status anzeigen |
| `/mcts menu` | GUI-Menü öffnen |
| `/mcts reload` | Skriptliste neu laden |

---

## 7. Hotkeys (Standard)

| Taste | Aktion |
|-------|--------|
| `K` | Skript-Menü öffnen |
| `Numpad 1–9` | Quick-Run Slot 1–9 |

Hotkeys können in **Optionen → Tastenbelegung → mc-typescript** umkonfiguriert werden.

---

## 8. Echo-Test (Verbindung prüfen)

```typescript
// scripts/echo-test.ts
import { mc } from 'mc-typescript'

const pos = await mc.player.pos()
console.log('Verbindung OK! Position:', pos)
await mc.chat.send(`Ich bin bei ${Math.round(pos.x)}, ${Math.round(pos.y)}, ${Math.round(pos.z)}`)
```

---

## ⚠️ Disclaimer

> Diese Mod/Library ist ausschließlich für **eigene Server** oder Server, auf denen du **ausdrücklich Erlaubnis** hast, solche Tools zu verwenden.  
> Auf öffentlichen Servern oder ohne Erlaubnis des Serverbetreibers kann die Nutzung gegen die Nutzungsbedingungen verstoßen.  
> **Nutzung auf eigene Verantwortung.**

---

## Projektstruktur

```
mc-typescript/
├── forge-mod/          # Java Forge Mod (MC 1.20.1, Forge 47.2.0)
├── ts-library/         # NPM-Package "mc-typescript"
├── scripts/            # Beispiel-Skripte
└── docs/               # Dokumentation
```

Weitere Details: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
