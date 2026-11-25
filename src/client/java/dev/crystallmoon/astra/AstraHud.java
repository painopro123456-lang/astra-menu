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
    private static final int MUTED = 0xFFB9C0CB;
    private static final int ACCENT = 0xFF9DBBEE;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    private AstraHud() {}

    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        int x = 8;
        int y = 8;
        if (AstraModules.isEnabled("Coordinates")) {
            graphics.fill(x, y, x + 142, y + 34, BACKGROUND);
            graphics.text(client.font, "XYZ", x + 7, y + 5, ACCENT);
            graphics.text(client.font, String.format("%.1f  %.1f  %.1f", client.player.getX(), client.player.getY(), client.player.getZ()), x + 7, y + 19, WHITE);
            y += 40;
        }
        if (AstraModules.isEnabled("FPS Counter")) {
            drawLine(graphics, client, x, y, 94, "FPS  " + client.getFps());
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
            drawLine(graphics, client, x, y, 116, "Facing  " + direction);
            y += 24;
        }
        if (AstraModules.isEnabled("Clock")) {
            drawLine(graphics, client, x, y, 100, CLOCK.format(LocalTime.now()));
            y += 24;
        }
        if (AstraModules.isEnabled("Health HUD")) {
            drawLine(graphics, client, x, y, 116, String.format("Health  %.1f / %.0f", client.player.getHealth(), client.player.getMaxHealth()));
            y += 24;
        }
        if (AstraModules.isEnabled("Ping Display") && client.getConnection() != null) {
            var info = client.getConnection().getPlayerInfo(client.player.getUUID());
            if (info != null) {
                drawLine(graphics, client, x, y, 100, "Ping  " + info.getLatency() + " ms");
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
            drawLine(graphics, client, x, y, 160, armor.toString());
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
            drawLine(graphics, client, x, y, 110, "Held item  " + (held.isEmpty() ? "None" : held.getCount()));
        }
    }

    private static void drawLine(GuiGraphicsExtractor graphics, Minecraft client, int x, int y, int width, String text) {
        graphics.fill(x, y, x + width, y + 20, BACKGROUND);
        graphics.text(client.font, text, x + 7, y + 6, WHITE);
    }

    private static void drawKey(GuiGraphicsExtractor graphics, Minecraft client, int x, int y, String key, boolean pressed) {
        int color = pressed ? 0xFF789BD0 : 0xCC171B22;
        graphics.fill(x, y, x + 22, y + 20, color);
        graphics.text(client.font, key, x + 7, y + 6, WHITE);
    }
}
