package dev.crystallmoon.dfufast;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs startup checkpoints so DFU Fast can be measured on real hardware.
 */
public final class DfuFast implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("DFU Fast");
    private static final AtomicBoolean TITLE_SCREEN_REPORTED = new AtomicBoolean();

    @Override
    public void onInitializeClient() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        LOGGER.info("Startup checkpoint: Fabric client initialization began {} ms after JVM start",
                uptimeMs);
    }

    public static void onTitleScreenDisplayed() {
        if (!TITLE_SCREEN_REPORTED.compareAndSet(false, true)) {
            return;
        }

        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        LOGGER.info("Startup complete: Minecraft title screen displayed {} ms after JVM start",
                uptimeMs);
    }
}
