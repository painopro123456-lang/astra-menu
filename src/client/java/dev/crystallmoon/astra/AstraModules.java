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
        // Fresh module registry: old placeholder modules intentionally removed.
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
