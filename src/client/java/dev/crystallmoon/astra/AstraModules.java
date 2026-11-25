package dev.crystallmoon.astra;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AstraModules {
    private static final Map<String, Boolean> STATES = new LinkedHashMap<>();

    static {
        STATES.put("Coordinates", true);
        STATES.put("Keystrokes", true);
        STATES.put("Armor Status", false);
        STATES.put("FPS Counter", true);
        STATES.put("CPS Counter", false);
        STATES.put("Direction", true);
        STATES.put("Status Effects", false);
        STATES.put("Toggle Sprint", false);
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
