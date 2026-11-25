package dev.crystallmoon.astra;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class AstraHud {
    private static final int BACKGROUND = 0x990D1016;
    private static final int WHITE = 0xFFF2F3F5;
    private static final int MUTED = 0xFFB9C0CB;
    private static final int ACCENT = 0xFF9DBBEE;

    private AstraHud() {}

    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int x = 8;
        int y = 8;
        if (AstraModules.isEnabled("Coordinates")) {
            graphics.fill(x, y, x + 126, y + 34, BACKGROUND);
            graphics.text(client.font, "XYZ", x + 7, y + 5, ACCENT);
            graphics.text(client.font, String.format("%.1f  %.1f  %.1f", client.player.getX(), client.player.getY(), client.player.getZ()), x + 7, y + 19, WHITE);
            y += 40;
        }
        if (AstraModules.isEnabled("FPS Counter")) {
            graphics.fill(x, y, x + 94, y + 20, BACKGROUND);
            graphics.text(client.font, "FPS  " + client.getFps(), x + 7, y + 6, WHITE);
            y += 26;
        }
        if (AstraModules.isEnabled("Direction")) {
            float yaw = client.player.getYRot();
            String direction = switch (Math.floorMod(Math.round(yaw / 45.0f), 8)) {
                case 0 -> "South";
                case 1 -> "South-West";
                case 2 -> "West";
                case 3 -> "North-West";
                case 4 -> "North";
                case 5 -> "North-East";
                case 6 -> "East";
                default -> "South-East";
            };
            graphics.fill(x, y, x + 112, y + 20, BACKGROUND);
            graphics.text(client.font, "Facing  " + direction, x + 7, y + 6, MUTED);
        }
    }
}
