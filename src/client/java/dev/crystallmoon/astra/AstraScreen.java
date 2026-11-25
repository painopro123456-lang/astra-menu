package dev.crystallmoon.astra;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AstraScreen extends Screen {
    private static final int PANEL = 0xF0181A1F;
    private static final int PANEL_ALT = 0xFF22252B;
    private static final int ACCENT = 0xFF8DB5FF;
    private static final int TEXT = 0xFFF1F3F6;
    private static final int MUTED = 0xFF9DA4AF;

    public AstraScreen() {
        super(Component.literal("Astra Menu"));
    }

    @Override
    protected void init() {
        // Layout is drawn relative to the current window size in render().
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int left = Math.max(18, (width - 520) / 2);
        int top = Math.max(18, (height - 330) / 2);
        int right = Math.min(width - 18, left + 520);
        int bottom = Math.min(height - 18, top + 330);

        graphics.fill(0, 0, width, height, 0x99000000);
        graphics.fill(left, top, right, bottom, PANEL);
        graphics.fill(left, top, right, top + 42, PANEL_ALT);
        graphics.fill(left, top, left + 3, bottom, ACCENT);
        graphics.drawString(font, "ASTRA", left + 18, top + 15, TEXT, false);
        graphics.drawString(font, "CLIENT  /  MOD MENU", left + 75, top + 16, MUTED, false);
        graphics.drawString(font, "RIGHT SHIFT  ·  CLOSE", right - 142, top + 16, MUTED, false);

        int contentTop = top + 60;
        graphics.drawString(font, "HUD MODULES", left + 18, contentTop, ACCENT, false);
        graphics.drawString(font, "Classic Minecraft-style overlays. Clean, compact and readable.", left + 18, contentTop + 16, MUTED, false);

        drawModule(graphics, left + 18, contentTop + 48, "Coordinates", "XYZ position display", true);
        drawModule(graphics, left + 18, contentTop + 91, "Keystrokes", "Traditional WASD + mouse buttons", true);
        drawModule(graphics, left + 18, contentTop + 134, "Armor Status", "Armor icons with durability values", true);
        drawModule(graphics, left + 18, contentTop + 177, "FPS Counter", "Simple vanilla-style FPS text", true);
        drawModule(graphics, left + 265, contentTop + 48, "CPS Counter", "Left and right click rate", false);
        drawModule(graphics, left + 265, contentTop + 91, "Direction", "Facing direction and cardinal point", false);
        drawModule(graphics, left + 265, contentTop + 134, "Status Effects", "Potion icons and remaining time", false);
        drawModule(graphics, left + 265, contentTop + 177, "Toggle Sprint", "Hold sprint without holding the key", false);

        graphics.drawString(font, "A first foundation build — module controls and HUD rendering are being added next.", left + 18, bottom - 27, MUTED, false);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private void drawModule(GuiGraphics graphics, int x, int y, String name, String description, boolean enabled) {
        graphics.fill(x, y, x + 228, y + 36, PANEL_ALT);
        graphics.drawString(font, name, x + 10, y + 6, TEXT, false);
        graphics.drawString(font, description, x + 10, y + 20, MUTED, false);
        graphics.fill(x + 207, y + 12, x + 218, y + 23, enabled ? 0xFF78B98A : 0xFF626873);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
