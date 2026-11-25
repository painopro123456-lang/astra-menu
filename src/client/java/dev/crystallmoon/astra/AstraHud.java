package dev.crystallmoon.astra;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public final class AstraHud {
    private static final int BACKGROUND = 0x990D1016;
    private static final int WHITE = 0xFFF2F3F5;
    private static final int ACCENT = 0xFF9DBBEE;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    private AstraHud() {}

    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int x = 8;
        int y = 8;
        if (AstraModules.isEnabled("Coordinates")) {
            drawLine(graphics, client, x, y, 170, String.format("XYZ  %.1f  %.1f  %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ()));
            y += 24;
        }
        if (AstraModules.isEnabled("FPS Counter")) {
            drawLine(graphics, client, x, y, 100, "FPS  " + client.getFps());
            y += 24;
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
            drawLine(graphics, client, x, y, 130, "Facing  " + direction);
            y += 24;
        }
        if (AstraModules.isEnabled("Clock")) {
            drawLine(graphics, client, x, y, 110, "Time  " + CLOCK.format(LocalTime.now()));
            y += 24;
        }
        if (AstraModules.isEnabled("Health HUD")) {
            drawLine(graphics, client, x, y, 145, String.format("Health  %.1f / %.0f",
                    client.player.getHealth(), client.player.getMaxHealth()));
            y += 24;
        }
        if (AstraModules.isEnabled("Hunger HUD")) {
            drawLine(graphics, client, x, y, 115, "Food  " + client.player.getFoodData().getFoodLevel() + " / 20");
            y += 24;
        }
        if (AstraModules.isEnabled("Saturation HUD")) {
            drawLine(graphics, client, x, y, 145, String.format("Saturation  %.1f",
                    client.player.getFoodData().getSaturationLevel()));
            y += 24;
        }
        if (AstraModules.isEnabled("Experience HUD")) {
            drawLine(graphics, client, x, y, 135, "XP  " + client.player.experienceLevel
                    + "  (" + Math.round(client.player.experienceProgress * 100) + "%)");
            y += 24;
        }
        if (AstraModules.isEnabled("Speedometer")) {
            double speed = client.player.getDeltaMovement().horizontalDistance() * 20.0;
            drawLine(graphics, client, x, y, 145, String.format("Speed  %.2f m/s", speed));
            y += 24;
        }
        if (AstraModules.isEnabled("World Time")) {
            long ticks = Math.floorMod(client.level.getLevelData().getGameTime(), 24000L);
            long hours = (ticks / 1000 + 6) % 24;
            long minutes = (ticks % 1000) * 60 / 1000;
            drawLine(graphics, client, x, y, 145, String.format("World  %02d:%02d", hours, minutes));
            y += 24;
        }
        if (AstraModules.isEnabled("Dimension")) {
            drawLine(graphics, client, x, y, 180, "Dimension  " + client.level.dimension().toString());
            y += 24;
        }
        if (AstraModules.isEnabled("Light Level")) {
            int light = client.level.getMaxLocalRawBrightness(client.player.blockPosition());
            drawLine(graphics, client, x, y, 130, "Block light  " + light);
            y += 24;
        }
        if (AstraModules.isEnabled("Fall Distance")) {
            drawLine(graphics, client, x, y, 135, String.format("Fall  %.1f blocks", client.player.fallDistance));
            y += 24;
        }
        if (AstraModules.isEnabled("Ping Display") && client.getConnection() != null) {
            var info = client.getConnection().getPlayerInfo(client.player.getUUID());
            if (info != null) {
                drawLine(graphics, client, x, y, 110, "Ping  " + info.getLatency() + " ms");
                y += 24;
            }
        }
        if (AstraModules.isEnabled("Armor Status")) {
            StringBuilder armor = new StringBuilder("Armor  ");
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack stack = client.player.getItemBySlot(slot);
                armor.append(stack.isEmpty() ? "--" : stack.getMaxDamage() > 0
                        ? Math.max(0, stack.getMaxDamage() - stack.getDamageValue()) : stack.getCount());
                armor.append("  ");
            }
            drawLine(graphics, client, x, y, 180, armor.toString());
            y += 24;
        }
        if (AstraModules.isEnabled("Keystrokes")) {
            int keyY = Math.max(8, client.getWindow().getGuiScaledHeight() - 58);
            int center = client.getWindow().getGuiScaledWidth() / 2;
            drawKey(graphics, client, center - 12, keyY, "W", client.options.keyUp.isDown());
            drawKey(graphics, client, center - 36, keyY + 23, "A", client.options.keyLeft.isDown());
            drawKey(graphics, client, center - 12, keyY + 23, "S", client.options.keyDown.isDown());
            drawKey(graphics, client, center + 12, keyY + 23, "D", client.options.keyRight.isDown());
            drawKey(graphics, client, center - 12, keyY + 46, "J", client.options.keyJump.isDown());
        }

        if (AstraModules.isEnabled("Item Counter")) {
            ItemStack held = client.player.getMainHandItem();
            drawLine(graphics, client, x, y, 125, "Held item  " + (held.isEmpty() ? "None" : held.getCount()));
            y += 24;
        }
        if (AstraModules.isEnabled("Block Coordinates")) {
            drawLine(graphics, client, x, y, 150, "Block  " + client.player.blockPosition().toShortString());
            y += 24;
        }
        if (AstraModules.isEnabled("Chunk Coordinates")) {
            var pos = client.player.blockPosition();
            drawLine(graphics, client, x, y, 150, "Chunk  " + (pos.getX() >> 4) + "  " + (pos.getZ() >> 4));
            y += 24;
        }
        if (AstraModules.isEnabled("Yaw Pitch")) {
            drawLine(graphics, client, x, y, 150, String.format("Yaw  %.1f   Pitch  %.1f",
                    client.player.getYRot(), client.player.getXRot()));
            y += 24;
        }
        if (AstraModules.isEnabled("Absorption")) {
            drawLine(graphics, client, x, y, 140, String.format("Absorption  %.1f",
                    client.player.getAbsorptionAmount()));
            y += 24;
        }
        if (AstraModules.isEnabled("Air HUD")) {
            drawLine(graphics, client, x, y, 120, "Air  " + client.player.getAirSupply()
                    + " / " + client.player.getMaxAirSupply());
            y += 24;
        }
        if (AstraModules.isEnabled("Memory Usage")) {
            Runtime runtime = Runtime.getRuntime();
            long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            long max = runtime.maxMemory() / (1024L * 1024L);
            drawLine(graphics, client, x, y, 145, "Memory  " + used + " / " + max + " MB");
            y += 24;
        }
        if (AstraModules.isEnabled("Java Version")) {
            drawLine(graphics, client, x, y, 170, "Java  " + System.getProperty("java.version"));
            y += 24;
        }
        if (AstraModules.isEnabled("Weather")) {
            drawLine(graphics, client, x, y, 125, "Weather  " + (client.level.isRaining() ? "Rain" : "Clear"));
            y += 24;
        }
        if (AstraModules.isEnabled("Day Counter") || AstraModules.isEnabled("World Age")) {
            long dayTime = client.level.getLevelData().getGameTime();
            if (AstraModules.isEnabled("Day Counter")) {
                drawLine(graphics, client, x, y, 120, "Day  " + (dayTime / 24000L + 1));
                y += 24;
            }
            if (AstraModules.isEnabled("World Age")) {
                drawLine(graphics, client, x, y, 145, "World age  " + dayTime + " ticks");
                y += 24;
            }
        }
        if (AstraModules.isEnabled("Biome")) {
            String biome = client.level.getBiome(client.player.blockPosition()).unwrapKey().map(k -> k.location().toString()).orElse("Unknown");
            drawLine(graphics, client, x, y, 190, "Biome  " + biome); y += 24;
        }
        if (AstraModules.isEnabled("Nether Coordinates")) {
            boolean nether = client.level.dimension().location().getPath().equals("the_nether");
            double scale = nether ? 8.0 : 0.125;
            drawLine(graphics, client, x, y, 190, "Nether XYZ  " + (int)(client.player.getX()*scale) + "  " + (int)client.player.getY() + "  " + (int)(client.player.getZ()*scale)); y += 24;
        }
        if (AstraModules.isEnabled("Real Time")) {
            drawLine(graphics, client, x, y, 130, "Real time  " + CLOCK.format(LocalTime.now())); y += 24;
        }
        if (AstraModules.isEnabled("Facing") || AstraModules.isEnabled("Compass")) {
            String[] points = {"S","SW","W","NW","N","NE","E","SE"};
            String point = points[Math.floorMod(Math.round(client.player.getYRot()/45.0f), 8)];
            drawLine(graphics, client, x, y, 130, "Compass  " + point); y += 24;
        }
        if (AstraModules.isEnabled("Held Item")) {
            ItemStack held = client.player.getMainHandItem();
            drawLine(graphics, client, x, y, 190, "Held  " + (held.isEmpty() ? "Empty" : held.getHoverName().getString())); y += 24;
        }
        if (AstraModules.isEnabled("Offhand Item")) {
            ItemStack held = client.player.getOffhandItem();
            drawLine(graphics, client, x, y, 190, "Offhand  " + (held.isEmpty() ? "Empty" : held.getHoverName().getString())); y += 24;
        }
        if (AstraModules.isEnabled("Rain Status")) {
            drawLine(graphics, client, x, y, 130, "Rain  " + (client.level.isRaining() ? "Yes" : "No")); y += 24;
        }
        if (AstraModules.isEnabled("Moon Phase")) {
            drawLine(graphics, client, x, y, 130, "Moon phase  " + client.level.getMoonPhase()); y += 24;
        }
        if (AstraModules.isEnabled("Client Version")) {
            drawLine(graphics, client, x, y, 160, "Minecraft  26.2"); y += 24;
        }
        if (AstraModules.isEnabled("Inventory Status") || AstraModules.isEnabled("Empty Slots")
                || AstraModules.isEnabled("Totem Counter") || AstraModules.isEnabled("Arrow Counter")
                || AstraModules.isEnabled("Food Counter") || AstraModules.isEnabled("Potion Counter")) {
            int empty = 0, totems = 0, arrows = 0, food = 0, potions = 0;
            for (int i = 0; i < client.player.getInventory().getContainerSize(); i++) {
                ItemStack stack = client.player.getInventory().getItem(i);
                if (stack.isEmpty()) { empty++; continue; }
                String item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                if (item.equals("totem_of_undying")) totems += stack.getCount();
                if (item.contains("arrow")) arrows += stack.getCount();
                if (item.contains("potion")) potions += stack.getCount();
                if (stack.getFoodProperties(client.player) != null) food += stack.getCount();
            }
            if (AstraModules.isEnabled("Inventory Status") || AstraModules.isEnabled("Empty Slots")) { drawLine(graphics, client, x, y, 155, "Empty slots  " + empty); y += 24; }
            if (AstraModules.isEnabled("Totem Counter")) { drawLine(graphics, client, x, y, 125, "Totems  " + totems); y += 24; }
            if (AstraModules.isEnabled("Arrow Counter")) { drawLine(graphics, client, x, y, 125, "Arrows  " + arrows); y += 24; }
            if (AstraModules.isEnabled("Food Counter")) { drawLine(graphics, client, x, y, 125, "Food  " + food); y += 24; }
            if (AstraModules.isEnabled("Potion Counter")) { drawLine(graphics, client, x, y, 125, "Potions  " + potions); y += 24; }
        }
        if (AstraModules.isEnabled("Status Effects") || AstraModules.isEnabled("Potion Timers")
                || AstraModules.isEnabled("Effect Icons") || AstraModules.isEnabled("Effect Duration")) {
            int active = 0;
            for (var effect : client.player.getActiveEffects()) active++;
            drawLine(graphics, client, x, y, 155, "Active effects  " + active); y += 24;
        }
        if (AstraModules.isEnabled("Toggle Sprint")) {
            drawLine(graphics, client, x, y, 130, "Sprint  " + (client.player.isSprinting() ? "ON" : "OFF")); y += 24;
        }
        if (AstraModules.isEnabled("Difficulty")) {
            drawLine(graphics, client, x, y, 145, "Difficulty  " + client.level.getDifficulty());
            y += 24;
        }
    }

    private static void drawLine(GuiGraphicsExtractor graphics, Minecraft client, int x, int y, int width, String text) {
        graphics.fill(x, y, x + width, y + 20, BACKGROUND);
        graphics.fill(x, y, x + 2, y + 20, ACCENT);
        graphics.text(client.font, text, x + 7, y + 6, WHITE);
    }

    private static void drawKey(GuiGraphicsExtractor graphics, Minecraft client, int x, int y, String key, boolean pressed) {
        int color = pressed ? 0xFF789BD0 : 0xCC171B22;
        graphics.fill(x, y, x + 22, y + 20, color);
        graphics.text(client.font, key, x + 7, y + 6, WHITE);
    }
}
