package dev.crystallmoon.astra;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Astra's curated module registry.
 *
 * Every entry here has a corresponding implementation in AstraHud.
 * Keep this list honest: do not register a module until its behaviour exists.
 */
public final class AstraModules {
    private static final Map<String, Boolean> STATES = new LinkedHashMap<>();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("astra-menu.properties");

    static {
        add("FPS Counter", false); // HUD
        add("Coordinates", false); // HUD
        add("Direction", false); // HUD
        add("Clock", false); // HUD
        add("Ping Display", false); // HUD
        add("CPS Counter", false); // HUD
        add("Keystrokes", false); // HUD
        add("Armor Status", false); // HUD
        add("Health HUD", false); // HUD
        add("Hunger HUD", false); // HUD
        add("Experience HUD", false); // HUD
        add("Speedometer", false); // HUD
        add("Status Effects", false); // HUD
        add("Potion Timers", false); // HUD
        add("Jump Height", false); // HUD
        add("Sprint Status", false); // Player
        add("Movement Speed", false); // Player
        add("Velocity", false); // Player
        add("Elytra Speed", false); // Player
        add("Vehicle HUD", false); // Player
        add("Game Mode", false); // Player
        add("World Time", false); // World
        add("Dimension", false); // World
        add("Light Level", false); // World
        add("Day Counter", false); // World
        add("Biome", false); // World
        add("Nether Coordinates", false); // World
        add("Weather", false); // World
        add("Moon Phase", false); // World
        add("Difficulty", false); // World
        add("Spawnable Light", false); // World
        add("Block Info", false); // World
        add("Mining Speed", false); // World
        add("Item Counter", false); // Items
        add("Inventory Status", false); // Items
        add("Empty Slots", false); // Items
        add("Totem Counter", false); // Items
        add("Arrow Counter", false); // Items
        add("Potion Counter", false); // Items
        add("Durability", false); // Items
        add("Armor Durability", false); // Items
        add("Hotbar", false); // Items
        add("Crosshair", false); // Visual
        add("Effect Icons", false); // Visual
        add("Target HUD", false); // Visual
    }

    private AstraModules() {}

    private static void add(String name, boolean enabled) {
        STATES.put(name, enabled);
    }

    /** Loads saved toggles while retaining defaults for newly added modules. */
    public static void load() {
        if (!Files.isRegularFile(CONFIG_FILE)) return;

        Properties saved = new Properties();
        try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
            saved.load(reader);
            STATES.replaceAll((name, defaultValue) -> {
                String value = saved.getProperty(name);
                return value == null ? defaultValue : Boolean.parseBoolean(value);
            });
        } catch (IOException exception) {
            System.err.println("[Astra] Could not load module settings: " + exception.getMessage());
        }
    }

    public static boolean isEnabled(String name) {
        return STATES.getOrDefault(name, false);
    }

    public static void toggle(String name) {
        if (!STATES.containsKey(name)) return;
        STATES.put(name, !STATES.get(name));
        save();
    }

    public static Map<String, Boolean> all() {
        return java.util.Collections.unmodifiableMap(STATES);
    }

    private static void save() {
        Properties saved = new Properties();
        STATES.forEach((name, enabled) -> saved.setProperty(name, Boolean.toString(enabled)));
        try {
            Files.createDirectories(CONFIG_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                saved.store(writer, "Astra Menu module states");
            }
        } catch (IOException exception) {
            System.err.println("[Astra] Could not save module settings: " + exception.getMessage());
        }
    }
}
