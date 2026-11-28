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
    private static final String[] CATEGORIES = {"All", "HUD", "Player", "World", "Items", "Visual", "Utility"};
    private static final int ROW_STEP = 46;
    private static final int BUTTON_HEIGHT = 34;

    private String category = "All";
    private int page;

    public AstraScreen() {
        super(Component.literal("Astra Menu"));
    }

    private int panelWidth() { return Math.max(0, Math.min(900, width - 24)); }
    private int panelHeight() { return Math.max(0, Math.min(520, height - 24)); }
    private int panelLeft() { return (width - panelWidth()) / 2; }
    private int panelTop() { return (height - panelHeight()) / 2; }
    private int sidebarWidth() { return Math.min(174, Math.max(104, panelWidth() / 5)); }
    private int categoryStep() {
        int available = Math.max(CATEGORIES.length * 22, panelHeight() - 112);
        return Math.min(34, available / CATEGORIES.length);
    }
    private int categoryButtonHeight() { return Math.max(18, Math.min(28, categoryStep() - 4)); }
    private int contentLeft() { return panelLeft() + sidebarWidth() + 16; }
    private int contentWidth() { return Math.max(1, panelLeft() + panelWidth() - contentLeft() - 16); }
    private int columns() { return contentWidth() >= 610 ? 2 : 1; }
    private int rowsPerPage() {
        int gridTop = panelTop() + 158;
        int gridBottom = panelTop() + panelHeight() - 62;
        return Math.max(1, (gridBottom - gridTop + 8) / ROW_STEP);
    }
    private int pageSize() { return rowsPerPage() * columns(); }

    @Override
    protected void init() {
        clearWidgets();
        int left = panelLeft();
        int top = panelTop();
        int side = sidebarWidth();
        int contentX = contentLeft();
        int colCount = columns();
        int colGap = colCount == 2 ? 12 : 0;
        int buttonWidth = Math.max(1, (contentWidth() - colGap) / colCount);

        for (int i = 0; i < CATEGORIES.length; i++) {
            String name = CATEGORIES[i];
            int step = categoryStep();
            int y = top + 92 + i * step;
            int buttonHeight = categoryButtonHeight();
            addRenderableWidget(Button.builder(
                    Component.literal((category.equals(name) ? "◆  " : "   ") + name), button -> {
                        category = name;
                        page = 0;
                        init();
                    }).bounds(left + 8, y, Math.max(1, side - 16), buttonHeight).build());
        }

        List<String> modules = filteredModules();
        int pageSize = pageSize();
        int pages = Math.max(1, (modules.size() + pageSize - 1) / pageSize);
        page = Math.max(0, Math.min(page, pages - 1));

        int start = page * pageSize;
        int end = Math.min(modules.size(), start + pageSize);
        for (int i = start; i < end; i++) {
            String name = modules.get(i);
            int local = i - start;
            int col = local % colCount;
            int row = local / colCount;
            int x = contentX + col * (buttonWidth + colGap);
            int y = top + 158 + row * ROW_STEP;
            boolean enabled = AstraModules.isEnabled(name);

            addRenderableWidget(Button.builder(
                    Component.literal((enabled ? "●  " : "○  ") + name), button -> {
                        AstraModules.toggle(name);
                        button.setMessage(Component.literal(
                                (AstraModules.isEnabled(name) ? "●  " : "○  ") + name));
                    }).bounds(x, y, buttonWidth, BUTTON_HEIGHT).build());
        }

        int navY = top + panelHeight() - 46;
        addRenderableWidget(Button.builder(Component.literal("‹"), button -> {
            page = Math.max(0, page - 1);
            init();
        }).bounds(contentX, navY, 34, 26).build());
        addRenderableWidget(Button.builder(Component.literal("›"), button -> {
            page = Math.min(pages - 1, page + 1);
            init();
        }).bounds(contentX + 40, navY, 34, 26).build());
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
                "Jump Height", "Saturation HUD", "Fall Distance", "Absorption", "Air HUD",
                "Health Bar", "Armor Points", "Attack Cooldown", "Yaw Pitch",
                "Coordinates HUD").contains(name)) return "HUD";

        if (List.of("Sprint Status", "Movement Speed", "Velocity", "Elytra Speed",
                "Vehicle HUD", "Game Mode", "Player Height", "Player Name",
                "Main Hand Durability", "Offhand Durability", "Total Armor Durability",
                "Player Food Level", "Player Air Supply", "Player Experience Level",
                "Player Rotation", "Player Ground Status", "Player Sneaking Status",
                "Horizontal Speed", "Vertical Speed", "Horse Speed", "Boat Speed",
                "Mount Health").contains(name)) return "Player";

        if (List.of("World Time", "Dimension", "Light Level", "Day Counter", "Biome",
                "Nether Coordinates", "Weather", "Moon Phase", "Difficulty",
                "Spawnable Light", "Block Info", "Mining Speed", "Block Coordinates",
                "Chunk Coordinates", "Chunk Section", "Region Coordinates", "Dimension ID",
                "Block Resistance", "Harvest Level", "Light Overlay", "Breaking Progress",
                "Target Block", "Looking At", "Target Coordinates", "Target Distance",
                "World Age", "Chunk Updates").contains(name)) return "World";

        if (List.of("Item Counter", "Inventory Status", "Empty Slots", "Totem Counter",
                "Arrow Counter", "Potion Counter", "Durability", "Armor Durability",
                "Hotbar", "Held Item", "Offhand Item", "Food Counter", "Tool Durability",
                "Held Item Name", "Held Item Count", "Offhand Item Name", "Offhand Item Count",
                "Selected Hotbar Slot", "Inventory Used Slots", "Inventory Fill Percentage",
                "Ender Pearl Counter", "Golden Apple Counter").contains(name)) return "Items";

        if (List.of("Crosshair", "Effect Icons", "Target HUD", "Arrow Trajectory",
                "Target Health", "Target Max Health", "Target Armor", "Target Absorption",
                "Target Name", "Target Type", "Target Entity ID", "Target Held Item",
                "Boss Bar").contains(name)) return "Visual";

        return "Utility";
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int left = panelLeft();
        int top = panelTop();
        int right = left + panelWidth();
        int bottom = top + panelHeight();
        int side = sidebarWidth();

        graphics.fill(0, 0, width, height, 0xA9000000);
        graphics.fill(left, top, right, bottom, BG);
        graphics.fill(left, top, right, top + Math.min(82, panelHeight()), PANEL);
        graphics.fill(left, top + Math.min(82, panelHeight()), left + side, bottom, PANEL);
        graphics.fill(left, top + 81, right, top + 83, ACCENT);
        graphics.fill(left + side - 1, top + 82, left + side + 1, bottom, 0x553F526B);

        graphics.text(font, "ASTRA", left + 14, top + 16, WHITE);
        graphics.text(font, "CLIENT", left + 83, top + 19, ACCENT);
        if (panelWidth() >= 520) {
            graphics.text(font, "Useful tools. No filler modules.", left + 14, top + 45, MUTED);
            graphics.text(font, "RIGHT SHIFT  /  CLOSE", Math.max(left + 180, right - 172), top + 22, MUTED);
        } else {
            graphics.text(font, "RIGHT SHIFT TO CLOSE", Math.max(left + 130, right - 142), top + 22, MUTED);
        }
        graphics.text(font, "LIBRARY", left + 14, top + 91, MUTED);

        int enabled = 0;
        for (boolean state : AstraModules.all().values()) if (state) enabled++;
        graphics.text(font, "MODULE LIBRARY", contentLeft(), top + 91, WHITE);
        if (contentWidth() >= 420) {
            graphics.text(font, enabled + " ENABLED  ·  " + AstraModules.all().size() + " AVAILABLE",
                    right - 255, top + 91, ACCENT);
        }

        List<String> modules = filteredModules();
        int pages = Math.max(1, (modules.size() + pageSize() - 1) / pageSize());
        graphics.text(font, category + "  /  " + modules.size() + " MODULES",
                contentLeft(), top + 116, WHITE);
        graphics.text(font, "Select a module to toggle it. Your choices save automatically.",
                contentLeft(), top + 136, MUTED);
        graphics.text(font, "PAGE " + (page + 1) + " OF " + pages,
                contentLeft() + 84, bottom - 37, MUTED);
        if (panelWidth() >= 620) {
            graphics.text(font, "ASTRA  •  BUILT FOR YOUR CLIENT", left + 14, bottom - 18, MUTED);
            graphics.text(font, AstraModules.all().size() + " MODULES REGISTERED",
                    right - 222, bottom - 18, MUTED);
        } else {
            graphics.text(font, AstraModules.all().size() + " MODULES", left + 14, bottom - 18, MUTED);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
