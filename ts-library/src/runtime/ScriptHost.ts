/**
 * ScriptHost – loads and executes TypeScript scripts from the scripts/ directory.
 *
 * Scripts are executed with tsx (TypeScript Execute) at runtime.
 * The host watches for file changes and hot-reloads scripts automatically.
 *
 * A script can either:
 *   - Be a plain async IIFE (immediately invoked function expression)
 *   - Export a default async function (called automatically)
 *   - Export a register({ mc, on }) function (called automatically)
 */

import * as path from 'path';
import * as fs from 'fs';
import { spawn, ChildProcess } from 'child_process';
import chokidar from 'chokidar';
import { mc } from '../index';

const SCRIPTS_DIR = path.resolve(process.cwd(), '..', 'scripts');
const WS_URL = process.env['MC_WS_URL'] ?? 'ws://localhost:8765';

/** Currently running script processes, keyed by script name */
const runningScripts: Map<string, ChildProcess> = new Map();

/** List of script names available in the scripts directory */
let availableScripts: string[] = [];

/** ── Utility ─────────────────────────────────────────────────────────────── */

function scriptName(filePath: string): string {
  return path.basename(filePath, path.extname(filePath));
}

function refreshAvailableScripts(): void {
  if (!fs.existsSync(SCRIPTS_DIR)) {
    availableScripts = [];
    return;
  }
  availableScripts = fs
    .readdirSync(SCRIPTS_DIR)
    .filter((f) => f.endsWith('.ts') || f.endsWith('.js'))
    .map((f) => scriptName(f));

  console.log('[ScriptHost] Available scripts:', availableScripts.join(', ') || '(none)');
}

/** ── Script runner ───────────────────────────────────────────────────────── */

export function runScript(name: string): void {
  const tsFile = path.join(SCRIPTS_DIR, `${name}.ts`);
  const jsFile = path.join(SCRIPTS_DIR, `${name}.js`);
  const file = fs.existsSync(tsFile) ? tsFile : jsFile;

  if (!fs.existsSync(file)) {
    console.error(`[ScriptHost] Script not found: ${name}`);
    return;
  }

  if (runningScripts.has(name)) {
    console.log(`[ScriptHost] Stopping existing instance of "${name}" before restart`);
    stopScript(name);
  }

  console.log(`[ScriptHost] Starting script: ${name}`);

  const child = spawn(
    'node',
    ['--require', 'tsx/cjs', file],
    {
      stdio: 'inherit',
      env: {
        ...process.env,
        MC_WS_URL: WS_URL,
        // Allow scripts to import 'mc-typescript' from the ts-library/src/index.ts
        NODE_PATH: path.resolve(__dirname, '..', '..'),
      },
    }
  );

  child.on('exit', (code) => {
    console.log(`[ScriptHost] Script "${name}" exited with code ${code}`);
    runningScripts.delete(name);
  });

  runningScripts.set(name, child);
}

export function stopScript(name: string): void {
  const child = runningScripts.get(name);
  if (!child) {
    console.log(`[ScriptHost] No running script: ${name}`);
    return;
  }
  child.kill('SIGTERM');
  runningScripts.delete(name);
  console.log(`[ScriptHost] Stopped script: ${name}`);
}

export function stopAllScripts(): void {
  for (const name of runningScripts.keys()) {
    stopScript(name);
  }
}

export function getAvailableScripts(): string[] {
  return [...availableScripts];
}

export function getRunningScripts(): string[] {
  return [...runningScripts.keys()];
}

/** ── WebSocket event routing (mod asks for script list, mod asks to run a script) */

function handleModMessage(msg: Record<string, unknown>): void {
  // The mod may request a list of available scripts for the in-game GUI
  if (msg['action'] === 'scriptHost.listScripts') {
    // Not a request/response pattern – just log
    console.log('[ScriptHost] Script list requested by mod');
  }
}

/** ── Entry point ─────────────────────────────────────────────────────────── */

async function main(): Promise<void> {
  refreshAvailableScripts();

  // Connect to the Forge mod
  try {
    await mc.connect(WS_URL);
    console.log('[ScriptHost] Connected to Forge mod at', WS_URL);
  } catch (err) {
    console.error('[ScriptHost] Could not connect to Forge mod:', (err as Error).message);
    console.log('[ScriptHost] Retrying in background…');
  }

  // Watch for script file changes (hot-reload)
  if (fs.existsSync(SCRIPTS_DIR)) {
    const watcher = chokidar.watch(SCRIPTS_DIR, { ignoreInitial: true });

    watcher.on('add', (filePath) => {
      console.log(`[ScriptHost] New script detected: ${scriptName(filePath)}`);
      refreshAvailableScripts();
    });

    watcher.on('change', (filePath) => {
      const name = scriptName(filePath);
      console.log(`[ScriptHost] Script changed: ${name}`);
      if (runningScripts.has(name)) {
        console.log(`[ScriptHost] Hot-reloading: ${name}`);
        runScript(name);
      }
    });

    watcher.on('unlink', (filePath) => {
      const name = scriptName(filePath);
      console.log(`[ScriptHost] Script removed: ${name}`);
      stopScript(name);
      refreshAvailableScripts();
    });
  }

  // Listen for mod events that trigger script execution
  mc.on('hotkey', (key) => {
    // Numpad 1–9 → run Quick-Run script at that slot
    const match = /^NUMPAD_([1-9])$/.exec(key);
    if (match) {
      const slot = parseInt(match[1], 10) - 1;
      const name = availableScripts[slot];
      if (name) runScript(name);
    }
  });

  console.log('[ScriptHost] Ready. Watching', SCRIPTS_DIR);
  console.log('[ScriptHost] Available scripts:', getAvailableScripts().join(', ') || '(none)');

  // Keep the process alive
  process.on('SIGINT', () => {
    console.log('\n[ScriptHost] Shutting down…');
    stopAllScripts();
    mc.disconnect();
    process.exit(0);
  });
}

main().catch(console.error);
