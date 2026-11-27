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

public final class AstraModules {
    private static final Map<String, Boolean> STATES = new LinkedHashMap<>();
    private static final Path CONFIG_FILE = FabricLoader.getInstance()
            .getConfigDir().resolve("astra-menu.properties");

    static {
        add("Coordinates", true);
        add("Keystrokes", true);
        add("Armor Status", false);
        add("FPS Counter", true);
        add("CPS Counter", false);
        add("Direction", true);
        add("Status Effects", false);
        add("Sprint Status", false);
        add("Clock", true);
        add("Health HUD", false);
        add("Ping Display", false);
        add("Item Counter", false);
        add("Hunger HUD", false);
        add("Saturation HUD", false);
        add("Experience HUD", false);
        add("Speedometer", false);
        add("World Time", false);
        add("Dimension", false);
        add("Light Level", false);
        add("Fall Distance", false);
        add("Biome", false);
        add("Chunk Coordinates", false);
        add("Block Coordinates", false);
        add("Nether Coordinates", false);
        add("Facing", false);
        add("Yaw Pitch", false);
        add("Compass", false);
        add("Time Played", false);
        add("Session Timer", false);
        add("Real Time", false);
        add("Durability", false);
        add("Held Item", false);
        add("Offhand Item", false);
        add("Hotbar", false);
        add("Inventory Status", false);
        add("Empty Slots", false);
        add("Totem Counter", false);
        add("Arrow Counter", false);
        add("Food Counter", false);
        add("Potion Counter", false);
        add("Entity Counter", false);
        add("Mob Counter", false);
        add("Player Counter", false);
        add("Boss Bar", false);
        add("Target HUD", false);
        add("Crosshair", false);
        add("Armor Durability", false);
        add("Health Bar", false);
        add("Absorption", false);
        add("Air HUD", false);
        add("Movement Speed", false);
        add("Jump Height", false);
        add("Velocity", false);
        add("Horizontal Speed", false);
        add("Vertical Speed", false);
        add("Elytra Speed", false);
        add("Horse Speed", false);
        add("Boat Speed", false);
        add("Mount Health", false);
        add("Vehicle HUD", false);
        add("Breaking Progress", false);
        add("Block Info", false);
        add("Target Block", false);
        add("Looking At", false);
        add("Block Resistance", false);
        add("Mining Speed", false);
        add("Tool Durability", false);
        add("Harvest Level", false);
        add("Light Overlay", false);
        add("Spawnable Light", false);
        add("Weather", false);
        add("Rain Status", false);
        add("Moon Phase", false);
        add("Day Counter", false);
        add("World Age", false);
        add("Difficulty", false);
        add("Game Mode", false);
        add("Server Address", false);
        add("Server Brand", false);
        add("Dimension ID", false);
        add("Coordinates HUD", false);
        add("FPS Graph", false);
        add("Ping Graph", false);
        add("Memory Usage", false);
        add("Java Version", false);
        add("Client Version", false);
        add("Server TPS", false);
        add("Packet Rate", false);
        add("Network Status", false);
        add("Chunk Updates", false);
        add("Nearby Players", false);
        add("Nearby Entities", false);
        add("Dropped Items", false);
        add("Projectile Counter", false);
        add("Arrow Trajectory", false);
        add("Potion Timers", false);
        add("Effect Icons", false);
        add("Effect Duration", false);
        add("Combat Timer", false);
        add("Last Damage", false);
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
