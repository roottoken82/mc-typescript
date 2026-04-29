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

---

## 2. Mod aus dem Quellcode bauen

### Voraussetzungen
- **Java Development Kit (JDK) 17** → [Download Adoptium/Temurin](https://adoptium.net/)
- Git (zum Klonen des Repos)

### Schritt-für-Schritt-Anleitung

```bash
# 1. Repository klonen
git clone https://github.com/Marco99999999999/mc-typescript.git
cd mc-typescript

# 2. In den Forge-Mod-Ordner wechseln
cd forge-mod

# 3. Mod bauen (Linux/macOS)
./gradlew build

# 3. Mod bauen (Windows)
gradlew.bat build
```

Das fertige JAR liegt anschließend unter:
```
forge-mod/build/libs/mc-typescript-1.0.0.jar
```

> **Hinweis:** Der erste Build lädt Minecraft-Abhängigkeiten herunter (~1 GB) und kann mehrere Minuten dauern.  
> Folgende Builds sind dank Gradle-Cache deutlich schneller.

### Installation der gebauten JAR
1. Die Datei `mc-typescript-1.0.0.jar` in den `mods`-Ordner kopieren:
   - Windows: `%APPDATA%\.minecraft\mods\`
   - macOS/Linux: `~/.minecraft/mods/`
2. Minecraft mit dem Forge-Profil starten

---

## 3. Mit Aternos-Server verbinden

Ganz normal! Keine extra Schritte nötig:

1. Minecraft starten → Multiplayer → Server-Adresse eintragen (z. B. `deinserver.aternos.me`)
2. Mit deinem Microsoft-Account einloggen (wie immer)
3. Die Mod läuft **in deinem Client** – der Aternos-Server merkt nichts davon
4. Im Spiel `/mcts enable` eingeben (Sicherheits-Toggle)

> **Hinweis:** Aternos-Server laufen als Vanilla/Paper. Auf dem Server ist **keine Mod nötig**.

---

## 4. TypeScript-Runtime starten

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

## 5. Eigenes Skript schreiben

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
- **In-Game:** `/tsmacro run mein-skript`
- **GUI:** `K` → Script-Manager öffnen → **Run** klicken
- **Terminal:** Runtime erkennt neue Dateien automatisch (Hot-Reload)

---

## 6. Verfügbare APIs

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

### `/mcts` – Mod-Verwaltung

| Befehl | Beschreibung |
|--------|-------------|
| `/mcts enable` | Mod aktivieren (Pflicht vor ersten Aktionen) |
| `/mcts disable` | Mod deaktivieren |
| `/mcts run <name>` | Skript starten |
| `/mcts stop` | Alle laufenden Skripte stoppen |
| `/mcts status` | Verbindungs-Status anzeigen |
| `/mcts menu` | Script-Manager-GUI öffnen |
| `/mcts reload` | Skript-Reload-Signal senden |

### `/tsmacro` – Skript-Steuerung (granular)

| Befehl | Beschreibung |
|--------|-------------|
| `/tsmacro list` | Alle Skripte mit Status auflisten |
| `/tsmacro run <name>` | Einzelnes Skript starten |
| `/tsmacro stop <name>` | Einzelnes Skript stoppen |
| `/tsmacro reload <name>` | Skript stoppen + mit neuer Version neu starten |
| `/tsmacro stopall` | Alle laufenden Skripte stoppen |
| `/tsmacro refresh` | Skriptliste vom Dateisystem neu einlesen |

> **Tipp:** Die `<name>`-Parameter entsprechen dem Dateinamen ohne Endung,  
> z. B. `/tsmacro run mein-skript` für `scripts/mein-skript.ts`.

---

## 7. Script-Manager-GUI

### Öffnen
- **Im Mods-Menü:** Hauptmenü → Mods → mc-typescript → **Config**
- **Hotkey:** `K` (öffnet den Script Manager direkt)
- **Befehl:** `/mcts menu` oder `/tsmacro menu`

### Funktionen
| Schaltfläche | Funktion |
|---|---|
| **Run** | Skript starten (sendet Signal an die TS-Runtime) |
| **Stop** | Skript anhalten |
| **Reload** | Skript stoppen + mit aktueller Dateiversion neu starten |
| **Aktualisieren** | Skriptliste vom Dateisystem neu laden |
| **Ordner öffnen** | Skript-Ordner im Datei-Explorer öffnen |

### Skript-Ordner
Skripte werden gesucht in:
```
<minecraft-Verzeichnis>/config/mc-typescript/scripts/
```
Der Ordner wird beim ersten Start automatisch angelegt.

---

## 8. Hotkeys (Standard)

| Taste | Aktion |
|-------|--------|
| `K` | Skript-Menü öffnen |
| `Numpad 1–9` | Quick-Run Slot 1–9 |

Hotkeys können in **Optionen → Tastenbelegung → mc-typescript** umkonfiguriert werden.

---

## 9. Echo-Test (Verbindung prüfen)

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
