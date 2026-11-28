package dev.crystallmoon.astra;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AstraScreen extends Screen {
    private static final int BG = 0xE910131A;
    private static final int PANEL = 0xF51B202A;
    private static final int ACCENT = 0xFF9BC7FF;
    private static final int WHITE = 0xFFF4F7FC;
    private static final int MUTED = 0xFF9AA7B8;
    private static final String[] CATEGORIES = {"All", "HUD", "Player", "World", "Items", "Visual"};
    private static final int PAGE_SIZE = 12;

    private String category = "All";
    private int page;

    public AstraScreen() {
        super(Component.literal("Astra Menu"));
    }

    @Override
    protected void init() {
        clearWidgets();
        int left = Math.max(12, (width - 900) / 2);
        int top = Math.max(12, (height - 520) / 2);

        for (int i = 0; i < CATEGORIES.length; i++) {
            String name = CATEGORIES[i];
            int y = top + 112 + i * 39;
            addRenderableWidget(Button.builder(
                    Component.literal((category.equals(name) ? "◆  " : "   ") + name), button -> {
                        category = name;
                        page = 0;
                        init();
                    }).bounds(left + 18, y, 145, 31).build());
        }

        List<String> modules = filteredModules();
        int pages = Math.max(1, (modules.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        page = Math.max(0, Math.min(page, pages - 1));

        for (int i = page * PAGE_SIZE; i < Math.min(modules.size(), (page + 1) * PAGE_SIZE); i++) {
            String name = modules.get(i);
            int local = i - page * PAGE_SIZE;
            int col = local % 2;
            int row = local / 2;
            int x = left + 190 + col * 330;
            int y = top + 157 + row * 55;
            boolean enabled = AstraModules.isEnabled(name);

            addRenderableWidget(Button.builder(
                    Component.literal((enabled ? "●  " : "○  ") + name), button -> {
                        AstraModules.toggle(name);
                        button.setMessage(Component.literal(
                                (AstraModules.isEnabled(name) ? "●  " : "○  ") + name));
                    }).bounds(x, y, 310, 38).build());
        }

        addRenderableWidget(Button.builder(Component.literal("‹"), button -> {
            page--;
            init();
        }).bounds(left + 190, top + 455, 38, 28).build());
        addRenderableWidget(Button.builder(Component.literal("›"), button -> {
            page++;
            init();
        }).bounds(left + 236, top + 455, 38, 28).build());
    }

    private List<String> filteredModules() {
        List<String> result = new ArrayList<>();
        for (String name : AstraModules.all().keySet()) {
            if (category.equals("All") || categoryOf(name).equals(category)) result.add(name);
        }
        return result;
    }

    private static String categoryOf(String name) {
        if (List.of("FPS Counter", "Coordinates", "Direction", "Clock", "Ping Display",
                "CPS Counter", "Keystrokes", "Armor Status", "Health HUD", "Hunger HUD",
                "Experience HUD", "Speedometer", "Status Effects", "Potion Timers",
                "Jump Height").contains(name)) return "HUD";
        if (List.of("Sprint Status", "Movement Speed", "Velocity", "Elytra Speed",
                "Vehicle HUD", "Game Mode").contains(name)) return "Player";
        if (List.of("World Time", "Dimension", "Light Level", "Day Counter", "Biome",
                "Nether Coordinates", "Weather", "Moon Phase", "Difficulty",
                "Spawnable Light", "Block Info", "Mining Speed").contains(name)) return "World";
        if (List.of("Item Counter", "Inventory Status", "Empty Slots", "Totem Counter",
                "Arrow Counter", "Potion Counter", "Durability", "Armor Durability",
                "Hotbar").contains(name)) return "Items";
        if (List.of("Crosshair", "Effect Icons", "Target HUD", "Target Health", "Target Max Health",
                "Target Armor", "Target Absorption", "Target Name", "Target Type", "Target Entity ID",
                "Target Held Item").contains(name)) return "Visual";
        return "All";
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = Math.max(12, (width - 900) / 2);
        int top = Math.max(12, (height - 520) / 2);
        int right = Math.min(width - 12, left + 900);
        int bottom = Math.min(height - 12, top + 520);

        graphics.fill(0, 0, width, height, 0xA9000000);
        graphics.fill(left, top, right, bottom, BG);
        graphics.fill(left, top, right, top + 82, PANEL);
        graphics.fill(left, top + 82, left + 174, bottom, PANEL);
        graphics.fill(left, top + 81, right, top + 83, ACCENT);
        graphics.fill(left + 173, top + 82, left + 175, bottom, 0x553F526B);

        graphics.text(font, "ASTRA", left + 22, top + 18, WHITE);
        graphics.text(font, "CLIENT", left + 91, top + 21, ACCENT);
        graphics.text(font, "Useful tools. No filler modules.", left + 22, top + 48, MUTED);
        graphics.text(font, "RIGHT SHIFT  /  CLOSE", right - 172, top + 24, MUTED);
        graphics.text(font, "LIBRARY", left + 22, top + 97, MUTED);

        int enabled = 0;
        for (boolean state : AstraModules.all().values()) if (state) enabled++;
        graphics.text(font, "MODULE LIBRARY", left + 190, top + 94, WHITE);
        graphics.text(font, enabled + " ENABLED  ·  " + AstraModules.all().size() + " AVAILABLE",
                right - 255, top + 94, ACCENT);

        List<String> modules = filteredModules();
        int pages = Math.max(1, (modules.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        graphics.text(font, category + "  /  " + modules.size() + " MODULES",
                left + 190, top + 119, WHITE);
        graphics.text(font, "Tap a module to turn it on or off. Settings save automatically.",
                left + 190, top + 137, MUTED);
        graphics.text(font, "PAGE " + (page + 1) + " OF " + pages, left + 190, bottom - 42, MUTED);
        graphics.text(font, "ASTRA  •  BUILT FOR YOUR CLIENT", left + 22, bottom - 24, MUTED);
        graphics.text(font, AstraModules.all().size() + " MODULES REGISTERED", right - 222, bottom - 24, MUTED);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
