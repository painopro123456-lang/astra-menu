package dev.crystallmoon.astra;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AstraModules {
    private static final Map<String, Boolean> STATES = new LinkedHashMap<>();

    static {
        add("Coordinates", true);
        add("Keystrokes", true);
        add("Armor Status", false);
        add("FPS Counter", true);
        add("CPS Counter", false);
        add("Direction", true);
        add("Status Effects", false);
        add("Toggle Sprint", false);
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
    }

    private static void add(String name, boolean enabled) {
        STATES.put(name, enabled);
    }

    private AstraModules() {}

    public static boolean isEnabled(String name) {
        return STATES.getOrDefault(name, false);
    }

    public static void toggle(String name) {
        STATES.computeIfPresent(name, (key, enabled) -> !enabled);
    }

    public static Map<String, Boolean> all() {
        return Map.copyOf(STATES);
    }
}
