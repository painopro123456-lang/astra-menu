package dev.crystallmoon.dfufast;

import java.lang.management.ManagementFactory;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reports a lightweight startup checkpoint. DFU initialization is deferred
 * separately by SharedConstantsMixin.
 */
public final class DfuFast implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("DFU Fast");

    @Override
    public void onInitializeClient() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        LOGGER.info("Startup checkpoint: Fabric client initialization began {} ms after JVM start",
                uptimeMs);
    }
}
