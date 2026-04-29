package com.marco.mcts;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the list of available TS/JS scripts and their runtime status.
 *
 * Scripts are scanned from:
 *   {@code <minecraft>/config/mc-typescript/scripts/}
 *
 * The directory is created automatically on first use.
 */
public class ScriptManager {

    public enum ScriptStatus { IDLE, RUNNING, ERROR }

    private static Path scriptsDir;
    private static volatile List<String> scripts = Collections.emptyList();
    private static final Map<String, ScriptStatus> statuses      = new ConcurrentHashMap<>();
    private static final Map<String, String>        errorMessages = new ConcurrentHashMap<>();

    // ──────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ──────────────────────────────────────────────────────────────────────────

    /** Initialise – call from commonSetup or clientSetup. */
    public static void init() {
        scriptsDir = FMLPaths.GAMEDIR.get()
                .resolve("config")
                .resolve("mc-typescript")
                .resolve("scripts");
        try {
            Files.createDirectories(scriptsDir);
            McTsMod.LOGGER.info("[ScriptManager] Scripts directory: {}", scriptsDir);
        } catch (IOException e) {
            McTsMod.LOGGER.warn("[ScriptManager] Could not create scripts directory: {}", e.getMessage());
        }
        refresh();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Script discovery
    // ──────────────────────────────────────────────────────────────────────────

    /** Scan the scripts directory and refresh the cached list. */
    public static void refresh() {
        if (scriptsDir == null || !Files.exists(scriptsDir)) {
            scripts = Collections.emptyList();
            return;
        }

        List<String> found = new ArrayList<>();
        try {
            Files.walkFileTree(scriptsDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".ts") || fileName.endsWith(".js")) {
                        String relative = scriptsDir.relativize(file).toString()
                                .replace(File.separatorChar, '/');
                        int dot = relative.lastIndexOf('.');
                        if (dot > 0) relative = relative.substring(0, dot);
                        found.add(relative);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            McTsMod.LOGGER.warn("[ScriptManager] Error scanning scripts: {}", e.getMessage());
        }

        Collections.sort(found);
        scripts = Collections.unmodifiableList(found);
        McTsMod.LOGGER.info("[ScriptManager] Found {} script(s).", scripts.size());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Accessors
    // ──────────────────────────────────────────────────────────────────────────

    /** All script names (relative path without extension), sorted. */
    public static List<String> getScripts() {
        return scripts;
    }

    /** The scripts directory path (may be null before {@link #init()}). */
    public static Path getScriptsDir() {
        return scriptsDir;
    }

    /** Current runtime status for a script. */
    public static ScriptStatus getStatus(String name) {
        return statuses.getOrDefault(name, ScriptStatus.IDLE);
    }

    /** Error message for a script (only meaningful when status == ERROR). */
    public static String getErrorMessage(String name) {
        return errorMessages.getOrDefault(name, "");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Mutators (called by commands, GUI and MessageRouter)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Update the runtime status of a script.
     *
     * @param name   script name (relative path without extension)
     * @param status new status
     * @param error  optional error message (pass {@code null} to clear)
     */
    public static void setStatus(String name, ScriptStatus status, String error) {
        statuses.put(name, status);
        if (error != null) {
            errorMessages.put(name, error);
        } else {
            errorMessages.remove(name);
        }
    }

    /** Reset all script statuses to IDLE (e.g. after stopall). */
    public static void resetAll() {
        statuses.clear();
        errorMessages.clear();
    }
}
