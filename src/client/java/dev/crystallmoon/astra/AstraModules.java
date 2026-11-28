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
        add("Saturation HUD", false); // HUD / utility
        add("Fall Distance", false); // HUD / utility
        add("Block Coordinates", false); // HUD / utility
        add("Chunk Coordinates", false); // HUD / utility
        add("Yaw Pitch", false); // HUD / utility
        add("Absorption", false); // HUD / utility
        add("Air HUD", false); // HUD / utility
        add("Memory Usage", false); // HUD / utility
        add("Java Version", false); // HUD / utility
        add("World Age", false); // HUD / utility
        add("Real Time", false); // HUD / utility
        add("Facing", false); // HUD / utility
        add("Compass", false); // HUD / utility
        add("Held Item", false); // HUD / utility
        add("Offhand Item", false); // HUD / utility
        add("Rain Status", false); // HUD / utility
        add("Client Version", false); // HUD / utility
        add("Food Counter", false); // HUD / utility
        add("Horizontal Speed", false); // HUD / utility
        add("Vertical Speed", false); // HUD / utility
        add("Horse Speed", false); // HUD / utility
        add("Boat Speed", false); // HUD / utility
        add("Mount Health", false); // HUD / utility
        add("Health Bar", false); // HUD / utility
        add("Network Status", false); // HUD / utility
        add("Dimension ID", false); // HUD / utility
        add("Time Played", false); // HUD / utility
        add("Session Timer", false); // HUD / utility
        add("FPS Graph", false); // HUD / utility
        add("Ping Graph", false); // HUD / utility
        add("Last Damage", false); // HUD / utility
        add("Combat Timer", false); // HUD / utility
        add("Boss Bar", false); // HUD / utility
        add("Server TPS", false); // HUD / utility
        add("Packet Rate", false); // HUD / utility
        add("Server Address", false); // HUD / utility
        add("Server Brand", false); // HUD / utility
        add("Nearby Players", false); // HUD / utility
        add("Entity Counter", false); // HUD / utility
        add("Mob Counter", false); // HUD / utility
        add("Nearby Entities", false); // HUD / utility
        add("Dropped Items", false); // HUD / utility
        add("Projectile Counter", false); // HUD / utility
        add("Target Block", false); // HUD / utility
        add("Looking At", false); // HUD / utility
        add("Tool Durability", false); // HUD / utility
        add("Player Counter", false); // HUD / utility
        add("Coordinates HUD", false); // HUD / utility
        add("Block Resistance", false); // HUD / utility
        add("Harvest Level", false); // HUD / utility
        add("Light Overlay", false); // HUD / utility
        add("Breaking Progress", false); // HUD / utility
        add("Arrow Trajectory", false); // HUD / utility
        add("Chunk Updates", false); // HUD / utility
        add("Attack Cooldown", false); // HUD / utility
        add("Armor Points", false); // HUD / utility
        add("Held Item Name", false); // HUD / utility
        add("Held Item Count", false); // HUD / utility
        add("Offhand Item Name", false); // HUD / utility
        add("Offhand Item Count", false); // HUD / utility
        add("Selected Hotbar Slot", false); // HUD / utility
        add("Inventory Used Slots", false); // HUD / utility
        add("Inventory Fill Percentage", false); // HUD / utility
        add("Ender Pearl Counter", false); // HUD / utility
        add("Golden Apple Counter", false); // HUD / utility
        add("Chunk Section", false); // HUD / utility
        add("Region Coordinates", false); // HUD / utility
        add("Target Distance", false); // HUD / utility
        add("Target Coordinates", false); // HUD / utility
        add("Player Height", false); // HUD / utility
        add("Player Name", false); // HUD / utility
        add("Main Hand Durability", false); // HUD / utility
        add("Offhand Durability", false); // HUD / utility
        add("Total Armor Durability", false); // HUD / utility
        add("Player Food Level", false); // HUD / utility
        add("Player Air Supply", false); // HUD / utility
        add("Player Experience Level", false); // HUD / utility
        add("Player Rotation", false); // HUD / utility
        add("Player Ground Status", false); // HUD / utility
        add("Player Sneaking Status", false); // HUD / utility
        add("Target Health", false); // Visual / target HUD
        add("Target Max Health", false); // Visual / target HUD
        add("Target Armor", false); // Visual / target HUD
        add("Target Absorption", false); // Visual / target HUD
        add("Target Name", false); // Visual / target HUD
        add("Target Type", false); // Visual / target HUD
        add("Target Entity ID", false); // Visual / target HUD
        add("Target Held Item", false); // Visual / target HUD
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
