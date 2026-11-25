package dev.crystallmoon.astra;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class AstraScreen extends Screen {
    private static final int PANEL = 0xF0181A1F;
    private static final int PANEL_ALT = 0xFF22252B;
    private static final int ACCENT = 0xFF8DB5FF;
    private static final int TEXT = 0xFFF1F3F6;
    private static final int MUTED = 0xFF9DA4AF;

    public AstraScreen() { super(Component.literal("Astra Menu")); }

    @Override
    protected void init() {
        int left = Math.max(18, (width - 760) / 2);
        int top = Math.max(18, (height - 330) / 2);
        String[] names = AstraModules.all().keySet().toArray(String[]::new);
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            int column = i % 3;
            int row = i / 3;
            int x = left + 18 + column * 244;
            int y = top + 108 + row * 43;
            addRenderableWidget(Button.builder(moduleLabel(name), button -> {
                AstraModules.toggle(name);
                button.setMessage(moduleLabel(name));
            }).bounds(x, y, 226, 36).build());
        }
    }

    private static Component moduleLabel(String name) {
        return Component.literal((AstraModules.isEnabled(name) ? "[ON]  " : "[OFF]  ") + name);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int left = Math.max(18, (width - 760) / 2);
        int top = Math.max(18, (height - 330) / 2);
        int right = Math.min(width - 18, left + 760);
        int bottom = Math.min(height - 18, top + 330);
        graphics.fill(0, 0, width, height, 0x99000000);
        graphics.fill(left, top, right, bottom, PANEL);
        graphics.fill(left, top, right, top + 42, PANEL_ALT);
        graphics.fill(left, top, left + 3, bottom, ACCENT);
        graphics.text(font, "ASTRA", left + 18, top + 15, TEXT);
        graphics.text(font, "CLIENT  /  MOD MENU", left + 75, top + 16, MUTED);
        graphics.text(font, "RIGHT SHIFT  ·  CLOSE", right - 142, top + 16, MUTED);
        graphics.text(font, "HUD MODULES", left + 18, top + 60, ACCENT);
        graphics.text(font, "Traditional Minecraft-style overlays. Click to enable or disable.", left + 18, top + 78, MUTED);
        graphics.text(font, "HUD elements appear in-game when the menu is closed.", left + 18, bottom - 25, MUTED);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
