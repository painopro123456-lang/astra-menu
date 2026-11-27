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
        // HUD (27)
        add("FPS", false);
        add("Coordinates", false);
        add("Direction", false);
        add("Clock", false);
        add("Ping", false);
        add("CPS", false);
        add("Keystrokes", false);
        add("Armor", false);
        add("Health", false);
        add("Hunger", false);
        add("Saturation", false);
        add("Experience", false);
        add("Speed", false);
        add("Velocity", false);
        add("Jump Height", false);
        add("Fall Distance", false);
        add("Status Effects", false);
        add("Potion Timers", false);
        add("Durability", false);
        add("Item Count", false);
        add("Totem Count", false);
        add("Arrow Count", false);
        add("Food Count", false);
        add("Entity Count", false);
        add("Player Count", false);
        add("Mob Count", false);
        add("Boss Bar", false);
        // Player (19)
        add("Movement Info", false);
        add("Sprint Indicator", false);
        add("Sneak Indicator", false);
        add("Flight Indicator", false);
        add("Elytra Info", false);
        add("Mount Info", false);
        add("Vehicle Speed", false);
        add("Reach Display", false);
        add("Attack Cooldown", false);
        add("Hurt Time", false);
        add("Last Hit", false);
        add("Combat Duration", false);
        add("Player Model Info", false);
        add("Skin Info", false);
        add("UUID Display", false);
        add("Game Mode Info", false);
        add("Player List", false);
        add("Nearby Players", false);
        add("Player Health", false);
        // World (21)
        add("World Clock", false);
        add("World Day", false);
        add("World Age", false);
        add("Biome Info", false);
        add("Weather Info", false);
        add("Moon Phase", false);
        add("Dimension Info", false);
        add("Light Level", false);
        add("Chunk Position", false);
        add("Region Position", false);
        add("Block Position", false);
        add("Nether Position", false);
        add("Facing Direction", false);
        add("Target Block Info", false);
        add("Block Resistance", false);
        add("Mining Progress", false);
        add("Mining Speed", false);
        add("Tool Suitability", false);
        add("Spawn Light", false);
        add("World Difficulty", false);
        add("World Seed Info", false);
        // Items (19)
        add("Held Item Info", false);
        add("Offhand Info", false);
        add("Hotbar Status", false);
        add("Inventory Slots", false);
        add("Empty Slot Count", false);
        add("Container Info", false);
        add("Shulker Preview", false);
        add("Bundle Preview", false);
        add("Item Rarity", false);
        add("Item Components", false);
        add("Item Enchantments", false);
        add("Durability Warning", false);
        add("Armor Durability", false);
        add("Tool Durability", false);
        add("Selected Slot", false);
        add("Stack Count", false);
        add("Food Item Info", false);
        add("Potion Item Info", false);
        add("Map Info", false);
        // Visual (20)
        add("Clean HUD", false);
        add("Minimal Boss Bars", false);
        add("Custom Crosshair", false);
        add("Damage Flash Control", false);
        add("Fire Overlay Control", false);
        add("Pumpkin Overlay Control", false);
        add("Vignette Control", false);
        add("Weather Particles", false);
        add("Entity Outlines", false);
        add("Dropped Item Labels", false);
        add("Name Tag Distance", false);
        add("Chat Timestamps", false);
        add("Scoreboard Timer", false);
        add("Subtitle History", false);
        add("Toast History", false);
        add("Screenshot Indicator", false);
        add("Camera Coordinates", false);
        add("FOV Display", false);
        add("Gamma Display", false);
        add("Particle Counter", false);
        // Network (19)
        add("Connection Status", false);
        add("Server Address", false);
        add("Server Brand", false);
        add("Server Latency", false);
        add("Latency Graph", false);
        add("Packet Statistics", false);
        add("Server TPS Estimate", false);
        add("Chunk Load Counter", false);
        add("Network Diagnostics", false);
        add("Connection Uptime", false);
        add("Reconnect Reminder", false);
        add("Server Version", false);
        add("Client Version", false);
        add("Java Runtime", false);
        add("Memory Monitor", false);
        add("Garbage Collection Info", false);
        add("Loaded Entity Count", false);
        add("World Render Distance", false);
        add("Chunk Update Rate", false);
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
