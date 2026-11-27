package dev.crystallmoon.astra;

import net.fabricmc.api.ClientModInitializer;

/** Minimal client entrypoint. Astra's previous menu, modules, HUD and mixins have been removed. */
public final class AstraMenuClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Intentionally empty: clean slate for a future rebuild.
    }
}
