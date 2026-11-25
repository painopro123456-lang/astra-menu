package dev.crystallmoon.astra;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class AstraMenuClient implements ClientModInitializer {
    private static KeyMapping openMenu;
    private static KeyMapping toggleSprintKey;
    private static boolean sprintToggled;

    @Override
    public void onInitializeClient() {
        AstraModules.load();

        openMenu = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.astra_menu.open",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC
        ));

        toggleSprintKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.astra_menu.toggle_sprint",
                GLFW.GLFW_KEY_UNKNOWN,
                KeyMapping.Category.MISC
        ));

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("astra_menu", "classic_hud"),
                AstraHud::extract
        );

        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.consumeClick()) {
                if (client.gui.screen() instanceof AstraScreen) {
                    client.gui.setScreen(null);
                } else if (client.player != null) {
                    client.gui.setScreen(new AstraScreen());
                }
            }
            if (AstraModules.isEnabled("Toggle Sprint") && client.player != null) {
                while (toggleSprintKey.consumeClick()) sprintToggled = !sprintToggled;
                boolean moving = client.options.keyUp.isDown() || client.options.keyLeft.isDown()
                        || client.options.keyDown.isDown() || client.options.keyRight.isDown();
                client.player.setSprinting(sprintToggled && moving);
            } else {
                sprintToggled = false;
            }
        });
    }
}
