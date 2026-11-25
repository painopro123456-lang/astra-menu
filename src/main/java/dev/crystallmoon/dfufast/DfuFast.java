package dev.crystallmoon.dfufast;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads selected DFU classes on a low-priority daemon thread without
 * initializing them. Vanilla remains responsible for all DFU initialization.
 */
public final class DfuFast implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("DFU Fast");

    private static final String[] PRELOAD_CLASSES = {
            "com.mojang.datafixers.DataFixer",
            "com.mojang.datafixers.DataFixerBuilder",
            "com.mojang.datafixers.DataFixerUpper",
            "com.mojang.datafixers.DSL",
            "com.mojang.datafixers.schemas.Schema",
            "com.mojang.datafixers.types.Type",
            "com.mojang.serialization.Dynamic",
            "net.minecraft.util.datafix.DataFixers"
    };

    @Override
    public void onInitializeClient() {
        Thread thread = new Thread(this::preload, "DFU-Fast-Preloader");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    private void preload() {
        long started = System.nanoTime();
        int loaded = 0;

        try {
            ClassLoader loader = DfuFast.class.getClassLoader();
            for (String className : PRELOAD_CLASSES) {
                try {
                    Class.forName(className, false, loader);
                    loaded++;
                } catch (ClassNotFoundException | LinkageError exception) {
                    LOGGER.debug("Skipped optional DFU class {}", className, exception);
                }
            }
            long elapsedMs = (System.nanoTime() - started) / 1_000_000L;
            LOGGER.info("Background preload checked {}/{} DFU classes in {} ms",
                    loaded, PRELOAD_CLASSES.length, elapsedMs);
        } catch (Throwable throwable) {
            LOGGER.warn("DFU background preload stopped; vanilla DFU behavior is unchanged", throwable);
        }
    }
}
