package dev.crystallmoon.dfufast;

import java.lang.management.ManagementFactory;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reports a startup timing checkpoint without competing with Minecraft's
 * own initialization work. This diagnostic does not modify DFU or world data.
 */
public final class DfuFast implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("DFU Fast");

    @Override
    public void onInitializeClient() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        LOGGER.info("Startup checkpoint: Fabric client initialization began {} ms after JVM start",
                uptimeMs);
        LOGGER.info("Diagnostic-only build: no DFU classes are preloaded or modified");
    }
}
