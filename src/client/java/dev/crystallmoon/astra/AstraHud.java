package dev.crystallmoon.astra;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

public final class AstraHud {
    private static final int BACKGROUND = 0x990D1016;
    private static final int WHITE = 0xFFF2F3F5;
    private static final int ACCENT = 0xFF9DBBEE;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final long SESSION_START_NANOS = System.nanoTime();
    private static double jumpStartY = Double.NaN;
    private static double jumpPeakY = Double.NaN;
    private static boolean leftWasDown;
    private static boolean rightWasDown;
    private static final Deque<Long> LEFT_CLICK_TIMES = new ArrayDeque<>();
    private static final Deque<Long> RIGHT_CLICK_TIMES = new ArrayDeque<>();
    private static final int[] FPS_SAMPLES = new int[32];
    private static final int[] PING_SAMPLES = new int[32];
    private static int graphIndex;
    private static long lastGraphSample;
    private static float previousHealth = -1.0f;
    private static float lastDamageAmount;
    private static long lastDamageNanos;
    private static int chunkUpdates;
    private static long lastServerGameTime = -1L;
    private static long lastServerTimeNanos;
    private static double estimatedServerTps = -1.0;
    private static UUID trackedPlayerId;
    private static Object trackedLevel;
    private static long worldSessionStartNanos;
    private static int hudContentRight;

    public static void noteServerTime(long gameTime) {
        long now = System.nanoTime();
        if (lastServerGameTime >= 0L && gameTime > lastServerGameTime && lastServerTimeNanos > 0L) {
            double elapsed = (now - lastServerTimeNanos) / 1_000_000_000.0;
            if (elapsed > 0.0) estimatedServerTps = Math.min(20.0, (gameTime - lastServerGameTime) / elapsed);
        }
        lastServerGameTime = gameTime;
        lastServerTimeNanos = now;
    }

    private AstraHud() {}

    public static void noteChunkUpdate() { chunkUpdates++; }

    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        long now = System.nanoTime();
        UUID currentPlayerId = client.player.getUUID();
        if (!currentPlayerId.equals(trackedPlayerId)) {
            trackedPlayerId = currentPlayerId;
            previousHealth = -1.0f;
            lastDamageAmount = 0.0f;
            lastDamageNanos = 0L;
            jumpStartY = Double.NaN;
            jumpPeakY = Double.NaN;
            leftWasDown = false;
            rightWasDown = false;
            LEFT_CLICK_TIMES.clear();
            RIGHT_CLICK_TIMES.clear();
        }
        if (client.level != trackedLevel) {
            trackedLevel = client.level;
            worldSessionStartNanos = now;
            previousHealth = -1.0f;
            lastDamageAmount = 0.0f;
            lastDamageNanos = 0L;
            jumpStartY = Double.NaN;
            jumpPeakY = Double.NaN;
        }

        if (client.player.onGround()) {
            jumpStartY = client.player.getY();
            jumpPeakY = jumpStartY;
        } else if (!Double.isNaN(jumpStartY)) {
            jumpPeakY = Math.max(jumpPeakY, client.player.getY());
        }

        float currentHealth = client.player.getHealth();
        if (previousHealth >= 0.0f && currentHealth < previousHealth) {
            lastDamageAmount = previousHealth - currentHealth;
            lastDamageNanos = now;
        }
        previousHealth = currentHealth;
        boolean inGameInput = client.gui.screen() == null;
        boolean leftDown = inGameInput && client.mouseHandler.isLeftPressed();
        boolean rightDown = inGameInput && client.mouseHandler.isRightPressed();
        if (leftDown && !leftWasDown) LEFT_CLICK_TIMES.addLast(now);
        if (rightDown && !rightWasDown) RIGHT_CLICK_TIMES.addLast(now);
        leftWasDown = leftDown;
        rightWasDown = rightDown;
        while (!LEFT_CLICK_TIMES.isEmpty() && now - LEFT_CLICK_TIMES.peekFirst() > 1_000_000_000L) LEFT_CLICK_TIMES.removeFirst();
        while (!RIGHT_CLICK_TIMES.isEmpty() && now - RIGHT_CLICK_TIMES.peekFirst() > 1_000_000_000L) RIGHT_CLICK_TIMES.removeFirst();
        if (now - lastGraphSample >= 250_000_000L) {
            FPS_SAMPLES[graphIndex] = client.getFps();
            var connection = client.getConnection();
            var playerInfo = connection == null ? null : connection.getPlayerInfo(client.player.getUUID());
            PING_SAMPLES[graphIndex] = playerInfo == null ? 0 : playerInfo.getLatency();
            graphIndex = (graphIndex + 1) % FPS_SAMPLES.length;
            lastGraphSample = now;
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();
        hudContentRight = screenWidth - (AstraModules.isEnabled("Keystrokes") ? 170 : 8);
        int layoutBottom = screenHeight - (AstraModules.isEnabled("Armor Status") ? 64 : 8)
                - (AstraModules.isEnabled("Hotbar") ? 30 : 0);
        int x = 8;
        int y = 8;
        if (AstraModules.isEnabled("Coordinates")) {
            if (x + 190 <= hudContentRight) {
                drawPanel(graphics, client, x, y, 190, "POSITION");
                graphics.text(client.font, String.format("X %.1f   Y %.1f   Z %.1f",
                        client.player.getX(), client.player.getY(), client.player.getZ()), x + 7, y + 15, WHITE);
            }
            y += 38;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("FPS Counter")) {
            drawMetric(graphics, client, x, y, 112, "FPS", Integer.toString(client.getFps()), client.getFps(), 240, ACCENT);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
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
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Clock")) {
            drawLine(graphics, client, x, y, 110, "Time  " + CLOCK.format(LocalTime.now()));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Health HUD")) {
            drawMeter(graphics, client, x, y, 150, "HEALTH", client.player.getHealth(),
                    client.player.getMaxHealth(), 0xFFE45B68);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Hunger HUD")) {
            drawMeter(graphics, client, x, y, 150, "HUNGER",
                    client.player.getFoodData().getFoodLevel(), 20.0, 0xFFE8B64F);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Saturation HUD")) {
            drawLine(graphics, client, x, y, 145, String.format("Saturation  %.1f",
                    client.player.getFoodData().getSaturationLevel()));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Experience HUD")) {
            if (x + 150 <= hudContentRight) {
                drawPanel(graphics, client, x, y, 150, "EXPERIENCE  •  LVL " + client.player.experienceLevel);
                graphics.fill(x + 7, y + 17, x + 143, y + 24, 0xFF343A45);
                graphics.fill(x + 7, y + 17, x + 7 + Math.round(136 * client.player.experienceProgress),
                        y + 24, 0xFF70D77C);
            }
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Speedometer")) {
            double speedKmh = client.player.getDeltaMovement().horizontalDistance() * 20.0 * 3.6;
            drawMetric(graphics, client, x, y, 150, "SPEED", String.format("%.1f km/h", speedKmh),
                    speedKmh, 45.0, ACCENT);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("World Time")) {
            long ticks = Math.floorMod(client.level.getDefaultClockTime(), 24000L);
            long hours = (ticks / 1000 + 6) % 24;
            long minutes = (ticks % 1000) * 60 / 1000;
            drawLine(graphics, client, x, y, 145, String.format("World  %02d:%02d", hours, minutes));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Dimension")) {
            drawLine(graphics, client, x, y, 180, "Dimension  " + client.level.dimension().toString());
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Light Level")) {
            int light = client.level.getBrightness(LightLayer.BLOCK, client.player.blockPosition());
            drawLine(graphics, client, x, y, 130, "Block light  " + light);
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Fall Distance")) {
            drawLine(graphics, client, x, y, 135, String.format("Fall  %.1f blocks", client.player.fallDistance));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Ping Display") && client.getConnection() != null) {
            var info = client.getConnection().getPlayerInfo(client.player.getUUID());
            if (info != null) {
                drawLine(graphics, client, x, y, 110, "Ping  " + info.getLatency() + " ms");
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Armor Status")) {
            int armorX = 8;
            int armorY = client.getWindow().getGuiScaledHeight() - 58;
            graphics.fill(armorX - 4, armorY - 4, armorX + 4 * 30 + 2, armorY + 48, BACKGROUND);
            EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (int i = 0; i < armorSlots.length; i++) {
                ItemStack stack = client.player.getItemBySlot(armorSlots[i]);
                int slotX = armorX + i * 30;
                graphics.fill(slotX, armorY, slotX + 26, armorY + 44, 0xAA202631);
                if (!stack.isEmpty()) {
                    graphics.item(stack, slotX + 5, armorY + 2);
                    if (stack.isDamageableItem()) {
                        int remaining = Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
                        String durability = Integer.toString(remaining);
                        int textX = slotX + Math.max(1, (26 - client.font.width(durability)) / 2);
                        graphics.text(client.font, durability, textX, armorY + 20, WHITE);
                        int barWidth = Math.max(0, 22 * remaining / stack.getMaxDamage());
                        int barColor = remaining * 2 < stack.getMaxDamage() ? 0xFFE45B5B
                                : remaining * 4 < stack.getMaxDamage() * 3 ? 0xFFE5B65B : 0xFF65D58A;
                        graphics.fill(slotX + 2, armorY + 39, slotX + 24, armorY + 42, 0xFF343A45);
                        if (barWidth > 0) graphics.fill(slotX + 2, armorY + 39,
                                slotX + 2 + barWidth, armorY + 42, barColor);
                    }
                } else {
                    graphics.text(client.font, "-", slotX + 11, armorY + 15, 0xFF8A93A3);
                }
            }
        }
        if (AstraModules.isEnabled("Keystrokes")) {
            // Keep the traditional key display in the upper-right corner so it
            // never collides with the vanilla/custom hotbar or Armor Status.
            int keyY = 8;
            int center = client.getWindow().getGuiScaledWidth() - 60;
            drawKey(graphics, client, center - 12, keyY, "W", client.options.keyUp.isDown());
            drawKey(graphics, client, center - 36, keyY + 23, "A", client.options.keyLeft.isDown());
            drawKey(graphics, client, center - 12, keyY + 23, "S", client.options.keyDown.isDown());
            drawKey(graphics, client, center + 12, keyY + 23, "D", client.options.keyRight.isDown());
            drawKey(graphics, client, center - 12, keyY + 46, "SPACE", client.options.keyJump.isDown(), 46);
            drawKey(graphics, client, center - 36, keyY + 69, "LMB",
                    inGameInput && client.mouseHandler.isLeftPressed(), 34);
            drawKey(graphics, client, center + 2, keyY + 69, "RMB",
                    inGameInput && client.mouseHandler.isRightPressed(), 34);
        }

        if (AstraModules.isEnabled("Item Counter")) {
            ItemStack held = client.player.getMainHandItem();
            drawLine(graphics, client, x, y, 125, "Held item  " + (held.isEmpty() ? "None" : held.getCount()));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Block Coordinates")) {
            drawLine(graphics, client, x, y, 150, "Block  " + client.player.blockPosition().toShortString());
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Chunk Coordinates")) {
            var pos = client.player.blockPosition();
            drawLine(graphics, client, x, y, 150, "Chunk  " + (pos.getX() >> 4) + "  " + (pos.getZ() >> 4));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Yaw Pitch")) {
            drawLine(graphics, client, x, y, 150, String.format("Yaw  %.1f   Pitch  %.1f",
                    client.player.getYRot(), client.player.getXRot()));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Absorption")) {
            drawLine(graphics, client, x, y, 140, String.format("Absorption  %.1f",
                    client.player.getAbsorptionAmount()));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Air HUD")) {
            drawLine(graphics, client, x, y, 120, "Air  " + client.player.getAirSupply()
                    + " / " + client.player.getMaxAirSupply());
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Memory Usage")) {
            Runtime runtime = Runtime.getRuntime();
            long used = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L);
            long max = runtime.maxMemory() / (1024L * 1024L);
            drawLine(graphics, client, x, y, 145, "Memory  " + used + " / " + max + " MB");
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Java Version")) {
            drawLine(graphics, client, x, y, 170, "Java  " + System.getProperty("java.version"));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Weather")) {
            drawLine(graphics, client, x, y, 125, "Weather  " + (client.level.isRaining() ? "Rain" : "Clear"));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Day Counter") || AstraModules.isEnabled("World Age")) {
            long dayTime = client.level.getDefaultClockTime();
            long worldAge = client.level.getLevelData().getGameTime();
            if (AstraModules.isEnabled("Day Counter")) {
                drawLine(graphics, client, x, y, 120, "Day  " + (dayTime / 24000L + 1));
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("World Age")) {
                drawLine(graphics, client, x, y, 145, "World age  " + worldAge + " ticks");
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Biome")) {
            String biome = client.level.getBiome(client.player.blockPosition()).unwrapKey()
                    .map(key -> key.identifier().toString()).orElse("Unknown");
            drawLine(graphics, client, x, y, 190, "Biome  " + biome); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Nether Coordinates")) {
            String dimensionId = client.level.dimension().toString();
            if (dimensionId.contains("the_nether") || dimensionId.contains("overworld")) {
                // Nether coordinates are 1:8 relative to the Overworld.
                double scale = dimensionId.contains("the_nether") ? 8.0 : 0.125;
                drawLine(graphics, client, x, y, 210, "Equivalent XYZ  "
                        + (int) (client.player.getX() * scale) + "  " + (int) client.player.getY()
                        + "  " + (int) (client.player.getZ() * scale));
            } else {
                drawLine(graphics, client, x, y, 210, "Equivalent XYZ  N/A in this dimension");
            }
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Real Time")) {
            drawLine(graphics, client, x, y, 130, "Real time  " + CLOCK.format(LocalTime.now())); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Facing") || AstraModules.isEnabled("Compass")) {
            String[] points = {"South","South-West","West","North-West","North","North-East","East","South-East"};
            String[] abbreviations = {"S","SW","W","NW","N","NE","E","SE"};
            int directionIndex = Math.floorMod(Math.round(client.player.getYRot() / 45.0f), 8);
            if (AstraModules.isEnabled("Facing")) {
                drawLine(graphics, client, x, y, 150, "Facing  " + points[directionIndex]);
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Compass")) {
                drawLine(graphics, client, x, y, 130, "Compass  " + abbreviations[directionIndex]);
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Held Item")) {
            ItemStack held = client.player.getMainHandItem();
            drawLine(graphics, client, x, y, 190, "Held  " + (held.isEmpty() ? "Empty" : held.getHoverName().getString())); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Offhand Item")) {
            ItemStack held = client.player.getOffhandItem();
            drawLine(graphics, client, x, y, 190, "Offhand  " + (held.isEmpty() ? "Empty" : held.getHoverName().getString())); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Rain Status")) {
            drawLine(graphics, client, x, y, 130, "Rain  " + (client.level.isRaining() ? "Yes" : "No")); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Moon Phase")) {
            String[] phases = {"Full moon", "Waning gibbous", "Last quarter", "Waning crescent",
                    "New moon", "Waxing crescent", "First quarter", "Waxing gibbous"};
            int phase = (int) Math.floorMod(client.level.getDefaultClockTime() / 24000L, 8L);
            drawLine(graphics, client, x, y, 150, "Moon  " + phases[Math.floorMod(phase, phases.length)]);
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Client Version")) {
            drawLine(graphics, client, x, y, 160, "Minecraft  26.2"); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Inventory Status") || AstraModules.isEnabled("Empty Slots")
                || AstraModules.isEnabled("Totem Counter") || AstraModules.isEnabled("Arrow Counter")
                || AstraModules.isEnabled("Food Counter") || AstraModules.isEnabled("Potion Counter")) {
            int empty = 0, occupied = 0, totems = 0, arrows = 0, food = 0, potions = 0;
            for (ItemStack stack : client.player.getInventory().getNonEquipmentItems()) {
                if (stack.isEmpty()) { empty++; continue; }
                occupied++;
                String item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                if (item.equals("totem_of_undying")) totems += stack.getCount();
                if (item.contains("arrow")) arrows += stack.getCount();
                if (item.contains("potion")) potions += stack.getCount();
                if (item.contains("bread") || item.contains("apple") || item.contains("carrot")
                        || item.contains("potato") || item.contains("beef") || item.contains("porkchop")
                        || item.contains("chicken") || item.contains("mutton") || item.contains("rabbit")
                        || item.contains("cod") || item.contains("salmon") || item.contains("melon")
                        || item.contains("cookie") || item.contains("cake") || item.contains("pie")
                        || item.contains("stew") || item.contains("soup") || item.contains("berries")
                        || item.contains("chorus_fruit") || item.contains("dried_kelp")
                        || item.contains("honey_bottle") || item.contains("tropical_fish")
                        || item.contains("pufferfish") || item.contains("rotten_flesh")
                        || item.contains("spider_eye")) food += stack.getCount();
                
            }
            if (AstraModules.isEnabled("Inventory Status")) { drawLine(graphics, client, x, y, 170, "Inventory  " + occupied + " / " + (occupied + empty) + " slots used"); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; } }
            if (AstraModules.isEnabled("Empty Slots")) { drawLine(graphics, client, x, y, 155, "Empty slots  " + empty); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; } }
            if (AstraModules.isEnabled("Totem Counter")) { drawLine(graphics, client, x, y, 125, "Totems  " + totems); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; } }
            if (AstraModules.isEnabled("Arrow Counter")) { drawLine(graphics, client, x, y, 125, "Arrows  " + arrows); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; } }
            if (AstraModules.isEnabled("Food Counter")) { drawLine(graphics, client, x, y, 125, "Food items  " + food); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; } }
            if (AstraModules.isEnabled("Potion Counter")) { drawLine(graphics, client, x, y, 125, "Potions  " + potions); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; } }
        }
        if (AstraModules.isEnabled("Status Effects") || AstraModules.isEnabled("Potion Timers") || AstraModules.isEnabled("Effect Icons")) {
            var effects = client.player.getActiveEffects();
            if (AstraModules.isEnabled("Status Effects")) {
                StringBuilder names = new StringBuilder("Effects  ");
                boolean firstEffect = true;
                if (effects.isEmpty()) names.append("None");
                for (var effect : effects) {
                    if (!firstEffect) names.append(", ");
                    firstEffect = false;
                    names.append(effect.getEffect().value().getDisplayName().getString());
                    if (effect.getAmplifier() > 0) names.append(" ").append(effect.getAmplifier() + 1);
                }
                drawLine(graphics, client, x, y, 240, names.toString());
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Effect Icons")) {
                if (effects.isEmpty()) {
                    drawLine(graphics, client, x, y, 180, "Effect icons  None");
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                } else {
                    for (var effect : effects) {
                        String effectName = effect.getEffect().value().getDisplayName().getString();
                        String iconText = effectName.isEmpty() ? "?" : effectName.substring(0, 1).toUpperCase(java.util.Locale.ROOT);
                        int amplifier = effect.getAmplifier() + 1;
                        int seconds = Math.max(0, effect.getDuration() / 20);
                        int iconColor = effect.getEffect().value().isBeneficial() ? 0xFF477F62 : 0xFF8A5360;
                        if (x + 214 <= hudContentRight) {
                            graphics.fill(x, y, x + 20, y + 20, iconColor);
                            graphics.text(client.font, iconText, x + 7, y + 6, WHITE);
                        }
                        drawLine(graphics, client, x + 24, y, 190,
                                effectName + (amplifier > 1 ? " " + amplifier : "") +
                                "  " + String.format("%d:%02d", seconds / 60, seconds % 60));
                        y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                        
                    }
                }
            }
            if (AstraModules.isEnabled("Potion Timers")) {
                if (effects.isEmpty()) {
                    drawLine(graphics, client, x, y, 180, "Potion timers  None");
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                } else {
                    for (var effect : effects) {
                        String name = effect.getEffect().value().getDisplayName().getString();
                        int seconds = Math.max(0, effect.getDuration() / 20);
                        String duration = String.format("%d:%02d", seconds / 60, seconds % 60);
                        drawLine(graphics, client, x, y, 210, name + "  " + duration);
                        y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                        
                    }
                }
            }
        }
        if (AstraModules.isEnabled("Sprint Status")) {
            drawLine(graphics, client, x, y, 130, "Sprint status  " + (client.player.isSprinting() ? "ON" : "OFF")); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Difficulty")) {
            drawLine(graphics, client, x, y, 145, "Difficulty  " + client.level.getDifficulty());
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Movement Speed")) {
            double speed = client.player.getDeltaMovement().length() * 20.0;
            drawLine(graphics, client, x, y, 165, String.format("Movement speed  %.2f m/s", speed));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Horizontal Speed")) {
            double speed = client.player.getDeltaMovement().horizontalDistance() * 20.0;
            drawLine(graphics, client, x, y, 165, String.format("Horizontal speed  %.2f m/s", speed));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Vertical Speed")) {
            drawLine(graphics, client, x, y, 140, String.format("Vertical speed  %.2f m/s",
                    client.player.getDeltaMovement().y * 20.0));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Velocity")) {
            var velocity = client.player.getDeltaMovement();
            drawLine(graphics, client, x, y, 190, String.format("Velocity  %.2f / %.2f / %.2f",
                    velocity.x * 20.0, velocity.y * 20.0, velocity.z * 20.0));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Elytra Speed") && client.player.isFallFlying()) {
            drawLine(graphics, client, x, y, 145, String.format("Elytra  %.2f m/s",
                    client.player.getDeltaMovement().horizontalDistance() * 20.0));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (client.player.getVehicle() != null) {
            var vehicle = client.player.getVehicle();
            String vehicleType = vehicle.getType().toString().toLowerCase(java.util.Locale.ROOT);
            double vehicleSpeed = vehicle.getDeltaMovement().horizontalDistance() * 20.0;
            if (AstraModules.isEnabled("Vehicle HUD")) {
                drawLine(graphics, client, x, y, 190, "Vehicle  " + vehicle.getType().toString());
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Horse Speed") && vehicleType.contains("horse")) {
                drawLine(graphics, client, x, y, 155, String.format("Horse speed  %.2f m/s", vehicleSpeed));
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Boat Speed") && vehicleType.contains("boat")) {
                drawLine(graphics, client, x, y, 155, String.format("Boat speed  %.2f m/s", vehicleSpeed));
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Mount Health") && vehicle instanceof net.minecraft.world.entity.LivingEntity living) {
                drawLine(graphics, client, x, y, 150, String.format("Mount health  %.1f / %.0f",
                        living.getHealth(), living.getMaxHealth()));
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Health Bar")) {
            int max = Math.max(1, Math.round(client.player.getMaxHealth()));
            int current = Math.max(0, Math.round(client.player.getHealth()));
            String bar = "♥ ".repeat(Math.min(current, 20)) + "♡ ".repeat(Math.max(0, Math.min(max, 20) - current));
            drawLine(graphics, client, x, y, 190, "Health  " + bar);
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Game Mode") && client.gameMode != null) {
            drawLine(graphics, client, x, y, 150, "Game mode  " + client.gameMode.getPlayerMode().getName());
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Network Status")) {
            drawLine(graphics, client, x, y, 155, "Network  " +
                    (client.getConnection() == null ? "Offline" : "Connected"));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Dimension ID")) {
            drawLine(graphics, client, x, y, 190, "Dimension ID  " + client.level.dimension().identifier());
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Time Played")) {
            long elapsed = Math.max(0L, (now - worldSessionStartNanos) / 1_000_000_000L);
            long hours = elapsed / 3600;
            long minutes = (elapsed % 3600) / 60;
            long seconds = elapsed % 60;
            drawLine(graphics, client, x, y, 170,
                    "World session  " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Session Timer")) {
            long elapsed = Math.max(0L, (now - SESSION_START_NANOS) / 1_000_000_000L);
            long hours = elapsed / 3600;
            long minutes = (elapsed % 3600) / 60;
            long seconds = elapsed % 60;
            drawLine(graphics, client, x, y, 170,
                    "Client session  " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Jump Height")) {
            double height = Double.isNaN(jumpStartY) ? 0.0 : Math.max(0.0, jumpPeakY - jumpStartY);
            drawLine(graphics, client, x, y, 145, String.format("Jump height  %.2f m", height));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("CPS Counter")) {
            if (x + 150 <= hudContentRight) {
                drawPanel(graphics, client, x, y, 150, "CLICKS PER SECOND");
                graphics.text(client.font, "LMB  " + LEFT_CLICK_TIMES.size(), x + 7, y + 15, WHITE);
                graphics.text(client.font, "RMB  " + RIGHT_CLICK_TIMES.size(), x + 78, y + 15, WHITE);
            }
            y += 38;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("FPS Graph")) {
            drawGraph(graphics, x, y, "FPS", FPS_SAMPLES, 240);
            y += 52;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Ping Graph")) {
            drawGraph(graphics, x, y, "PING", PING_SAMPLES, 240);
            y += 52;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Target HUD") && client.hitResult instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
            var target = entityHit.getEntity();
            drawLine(graphics, client, x, y, 220, "Target  " + target.getName().getString());
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            if (target instanceof net.minecraft.world.entity.LivingEntity living) {
                drawLine(graphics, client, x, y, 190, String.format("Target health  %.1f / %.1f",
                        living.getHealth(), living.getMaxHealth()));
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Last Damage") && lastDamageNanos > 0L) {
            long seconds = (now - lastDamageNanos) / 1_000_000_000L;
            drawLine(graphics, client, x, y, 170, String.format("Last damage  %.1f ( %ds ago )",
                    lastDamageAmount, seconds));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Combat Timer")) {
            long seconds = lastDamageNanos == 0L ? -1L : (now - lastDamageNanos) / 1_000_000_000L;
            drawLine(graphics, client, x, y, 160, seconds < 0 ? "Combat  No recent damage"
                    : "Combat  " + seconds + "s since hit");
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Boss Bar")) {
            var bossEvents = ((dev.crystallmoon.astra.mixin.AstraBossOverlayAccessor)
                    client.gui.hud.getBossOverlay()).astra$getEvents();
            if (bossEvents.isEmpty()) {
                drawLine(graphics, client, x, y, 180, "Boss bars  None");
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            } else {
                for (var boss : bossEvents.values()) {
                    drawLine(graphics, client, x, y, 220, String.format("%s  %.0f%%",
                            boss.getName().getString(), boss.getProgress() * 100.0f));
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                    
                }
            }
        }
        if (AstraModules.isEnabled("Server TPS")) {
            drawLine(graphics, client, x, y, 170, estimatedServerTps < 0.0
                    ? "Server TPS  Measuring..." : String.format("Server TPS  %.1f", estimatedServerTps));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Packet Rate")) {
            float packets = client.getConnection() == null ? 0.0f
                    : client.getConnection().getConnection().getAverageReceivedPackets();
            drawLine(graphics, client, x, y, 170, String.format("Packets/s  %.1f", packets));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Server Address")) {
            var server = client.getCurrentServer();
            drawLine(graphics, client, x, y, 210, "Server  " + (server == null ? "Singleplayer" : server.ip));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Server Brand")) {
            String brand = client.getConnection() == null ? null : client.getConnection().serverBrand();
            drawLine(graphics, client, x, y, 210, "Server brand  " + (brand == null || brand.isBlank() ? "Unknown" : brand));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Nearby Players")) {
            int count = (int) client.level.players().stream().filter(player -> player != client.player)
                    .filter(player -> player.distanceToSqr(client.player) <= 64.0 * 64.0).count();
            drawLine(graphics, client, x, y, 155, "Players nearby  " + count);
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Entity Counter") || AstraModules.isEnabled("Mob Counter")
                || AstraModules.isEnabled("Nearby Entities") || AstraModules.isEnabled("Dropped Items")
                || AstraModules.isEnabled("Projectile Counter")) {
            int entities = 0, mobs = 0, items = 0, projectiles = 0;
            for (var entity : client.level.entitiesForRendering()) {
                if (entity == client.player) continue;
                entities++;
                if (entity instanceof net.minecraft.world.entity.LivingEntity
                        && !(entity instanceof net.minecraft.world.entity.player.Player)) mobs++;
                if (entity instanceof net.minecraft.world.entity.item.ItemEntity) items++;
                if (entity instanceof net.minecraft.world.entity.projectile.Projectile) projectiles++;
            }
            if (AstraModules.isEnabled("Entity Counter")) {
                drawLine(graphics, client, x, y, 150, "Loaded entities  " + entities); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Nearby Entities")) {
                long nearby = 0;
                for (var entity : client.level.entitiesForRendering()) {
                    if (entity != client.player && entity.distanceToSqr(client.player) <= 32.0 * 32.0) nearby++;
                }
                drawLine(graphics, client, x, y, 150, "Nearby entities  " + nearby); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Mob Counter")) {
                drawLine(graphics, client, x, y, 120, "Living entities  " + mobs); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Dropped Items")) {
                drawLine(graphics, client, x, y, 120, "Dropped items  " + items); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Projectile Counter")) {
                drawLine(graphics, client, x, y, 120, "Projectiles  " + projectiles); y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Target Block") || AstraModules.isEnabled("Looking At")
                || AstraModules.isEnabled("Block Info")) {
            if (client.hitResult instanceof net.minecraft.world.phys.BlockHitResult hit) {
                var pos = hit.getBlockPos();
                var state = client.level.getBlockState(pos);
                drawLine(graphics, client, x, y, 220, "Looking at  " + state.getBlock().getName().getString());
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                if (AstraModules.isEnabled("Target Block") || AstraModules.isEnabled("Block Info")) {
                    drawLine(graphics, client, x, y, 220, "Block pos  " + pos.toShortString());
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
            } else if (AstraModules.isEnabled("Looking At")) {
                drawLine(graphics, client, x, y, 150, "Looking at  Nothing");
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Durability") || AstraModules.isEnabled("Tool Durability")) {
            ItemStack tool = client.player.getMainHandItem();
            if (!tool.isEmpty() && tool.isDamageableItem()) {
                drawLine(graphics, client, x, y, 180, "Tool durability  "
                        + Math.max(0, tool.getMaxDamage() - tool.getDamageValue()) + " / " + tool.getMaxDamage());
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            } else {
                drawLine(graphics, client, x, y, 150, "Tool durability  N/A");
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Armor Durability")) {
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack armor = client.player.getItemBySlot(slot);
                String label = slot.name().toLowerCase(java.util.Locale.ROOT);
                String value = armor.isEmpty() || !armor.isDamageableItem() ? "N/A"
                        : (armor.getMaxDamage() - armor.getDamageValue()) + " / " + armor.getMaxDamage();
                drawLine(graphics, client, x, y, 180, "Armor " + label + "  " + value);
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Hotbar")) {
            int slotSize = 22;
            int hotbarX = (client.getWindow().getGuiScaledWidth() - slotSize * 9) / 2;
            int hotbarY = client.getWindow().getGuiScaledHeight() - 30;
            for (int i = 0; i < 9; i++) {
                int slotX = hotbarX + i * slotSize;
                ItemStack stack = client.player.getInventory().getItem(i);
                boolean selected = i == client.player.getInventory().getSelectedSlot();
                graphics.fill(slotX, hotbarY, slotX + 20, hotbarY + 20,
                        selected ? 0xFF526D91 : 0xBB10141C);
                if (selected) {
                    graphics.fill(slotX, hotbarY, slotX + 20, hotbarY + 1, ACCENT);
                    graphics.fill(slotX, hotbarY + 19, slotX + 20, hotbarY + 20, ACCENT);
                }
                if (!stack.isEmpty()) {
                    graphics.item(stack, slotX + 2, hotbarY + 2);
                    if (stack.getCount() > 1) {
                        String count = Integer.toString(stack.getCount());
                        graphics.text(client.font, count,
                                slotX + 19 - client.font.width(count), hotbarY + 11, WHITE);
                    }
                }
            }
        }
        if (AstraModules.isEnabled("Player Counter")) {
            long players = client.level.players().stream().filter(player -> player != client.player).count();
            drawLine(graphics, client, x, y, 155, "Other players  " + players);
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Coordinates HUD")) {
            drawLine(graphics, client, x, y, 180, String.format("Position  %.1f, %.1f, %.1f",
                    client.player.getX(), client.player.getY(), client.player.getZ()));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Crosshair")) {
            int cx = client.getWindow().getGuiScaledWidth() / 2;
            int cy = client.getWindow().getGuiScaledHeight() / 2;
            graphics.fill(cx - 7, cy, cx - 3, cy + 1, WHITE);
            graphics.fill(cx + 3, cy, cx + 7, cy + 1, WHITE);
            graphics.fill(cx, cy - 7, cx + 1, cy - 3, WHITE);
            graphics.fill(cx, cy + 3, cx + 1, cy + 7, WHITE);
        }
        if (AstraModules.isEnabled("Block Resistance") || AstraModules.isEnabled("Mining Speed")
                || AstraModules.isEnabled("Harvest Level") || AstraModules.isEnabled("Light Overlay")) {
            if (client.hitResult instanceof net.minecraft.world.phys.BlockHitResult blockHit) {
                var pos = blockHit.getBlockPos();
                var state = client.level.getBlockState(pos);
                if (AstraModules.isEnabled("Block Resistance")) {
                    drawLine(graphics, client, x, y, 190, String.format("Blast resistance  %.1f",
                            state.getBlock().getExplosionResistance()));
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Mining Speed")) {
                    drawLine(graphics, client, x, y, 190, String.format("Mining speed  %.2f",
                            client.player.getDestroySpeed(state)));
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Breaking Progress")) {
                    boolean attacking = client.options.keyAttack.isDown();
                    var breaking = (dev.crystallmoon.astra.mixin.AstraGameModeAccessor) client.gameMode;
                    float progress = breaking.astra$isDestroying()
                            && breaking.astra$getDestroyBlockPos().equals(pos)
                            ? breaking.astra$getDestroyProgress() : 0.0f;
                    drawLine(graphics, client, x, y, 190, attacking && breaking.astra$isDestroying()
                            ? String.format("Breaking progress  %.0f%%", Math.min(100.0f, progress * 100.0f))
                            : "Breaking progress  Start mining");
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Harvest Level")) {
                    drawLine(graphics, client, x, y, 190, "Harvest tool  " +
                            (client.player.getMainHandItem().isCorrectToolForDrops(state) ? "Suitable" : "Not suitable"));
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Light Overlay")) {
                    int light = client.level.getMaxLocalRawBrightness(pos);
                    drawLine(graphics, client, x, y, 190, "Block light  " + light);
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
            } else if (AstraModules.isEnabled("Mining Speed")
                    || AstraModules.isEnabled("Block Resistance") || AstraModules.isEnabled("Harvest Level")
                    || AstraModules.isEnabled("Light Overlay")) {
                drawLine(graphics, client, x, y, 190, "Block details  Look at a block");
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Arrow Trajectory")
                && (client.player.getMainHandItem().getItem() instanceof net.minecraft.world.item.BowItem
                || client.player.getMainHandItem().getItem() instanceof net.minecraft.world.item.CrossbowItem)) {
            var forward = client.player.getViewVector(1.0f);
            var right = forward.cross(new net.minecraft.world.phys.Vec3(0.0, 1.0, 0.0)).normalize();
            var up = right.cross(forward).normalize();
            var position = client.player.getEyePosition().add(forward.scale(0.15));
            var velocity = forward.scale(3.0);
            int screenWidth = client.getWindow().getGuiScaledWidth();
            int screenHeight = client.getWindow().getGuiScaledHeight();
            double focal = screenHeight * 0.5 / Math.tan(Math.toRadians(35.0));
            for (int step = 0; step < 40; step++) {
                position = position.add(velocity);
                velocity = velocity.scale(0.99).add(0.0, -0.05, 0.0);
                var relative = position.subtract(client.player.getEyePosition());
                double depth = relative.dot(forward);
                if (depth <= 0.1) continue;
                int px = (int) Math.round(screenWidth * 0.5 + focal * relative.dot(right) / depth);
                int py = (int) Math.round(screenHeight * 0.5 - focal * relative.dot(up) / depth);
                if (px >= 2 && px < screenWidth - 2 && py >= 2 && py < screenHeight - 2) {
                    graphics.fill(px - 1, py - 1, px + 2, py + 2, ACCENT);
                }
            }
        }

        if (AstraModules.isEnabled("Attack Cooldown")) {
            drawMeter(graphics, client, x, y, 150, "ATTACK COOLDOWN",
                    client.player.getAttackStrengthScale(0.0f) * 100.0, 100.0, ACCENT);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Armor Points")) {
            drawMetric(graphics, client, x, y, 150, "ARMOR",
                    Integer.toString(client.player.getArmorValue()), client.player.getArmorValue(), 20.0, ACCENT);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Held Item Name") || AstraModules.isEnabled("Held Item Count")) {
            ItemStack heldStack = client.player.getMainHandItem();
            String heldName = heldStack.isEmpty() ? "Empty hand" : heldStack.getHoverName().getString();
            if (AstraModules.isEnabled("Held Item Name")) {
                drawLine(graphics, client, x, y, 210, "Held item  " + heldName);
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Held Item Count")) {
                drawLine(graphics, client, x, y, 150, "Held count  " + (heldStack.isEmpty() ? 0 : heldStack.getCount()));
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Offhand Item Name") || AstraModules.isEnabled("Offhand Item Count")) {
            ItemStack offhandStack = client.player.getOffhandItem();
            String offhandName = offhandStack.isEmpty() ? "Empty" : offhandStack.getHoverName().getString();
            if (AstraModules.isEnabled("Offhand Item Name")) {
                drawLine(graphics, client, x, y, 210, "Offhand  " + offhandName);
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Offhand Item Count")) {
                drawLine(graphics, client, x, y, 150, "Offhand count  " + (offhandStack.isEmpty() ? 0 : offhandStack.getCount()));
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Selected Hotbar Slot")) {
            drawMetric(graphics, client, x, y, 150, "SELECTED SLOT",
                    Integer.toString(client.player.getInventory().getSelectedSlot() + 1),
                    client.player.getInventory().getSelectedSlot() + 1, 9.0, ACCENT);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Inventory Used Slots") || AstraModules.isEnabled("Inventory Fill Percentage")) {
            int usedSlots = 0;
            int totalSlots = client.player.getInventory().getNonEquipmentItems().size();
            for (ItemStack stack : client.player.getInventory().getNonEquipmentItems()) if (!stack.isEmpty()) usedSlots++;
            if (AstraModules.isEnabled("Inventory Used Slots")) {
                drawLine(graphics, client, x, y, 160, "Used slots  " + usedSlots + " / " + totalSlots);
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Inventory Fill Percentage")) {
                drawMeter(graphics, client, x, y, 160, "INVENTORY",
                        usedSlots * 100.0 / Math.max(1, totalSlots), 100.0, ACCENT);
                y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Ender Pearl Counter") || AstraModules.isEnabled("Golden Apple Counter")) {
            int pearls = 0;
            int goldenApples = 0;
            for (ItemStack stack : client.player.getInventory().getNonEquipmentItems()) {
                if (stack.is(net.minecraft.world.item.Items.ENDER_PEARL)) pearls += stack.getCount();
                if (stack.is(net.minecraft.world.item.Items.GOLDEN_APPLE)
                        || stack.is(net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE)) goldenApples += stack.getCount();
            }
            if (AstraModules.isEnabled("Ender Pearl Counter")) {
                drawLine(graphics, client, x, y, 150, "Ender pearls  " + pearls);
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Golden Apple Counter")) {
                drawLine(graphics, client, x, y, 150, "Golden apples  " + goldenApples);
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
        }
        if (AstraModules.isEnabled("Chunk Section")) {
            drawLine(graphics, client, x, y, 150, "Chunk section  " + (client.player.blockPosition().getY() >> 4));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Region Coordinates")) {
            var regionPos = client.player.blockPosition();
            drawLine(graphics, client, x, y, 170, "Region  " + (regionPos.getX() >> 9) + ", " + (regionPos.getZ() >> 9));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Target Distance") || AstraModules.isEnabled("Target Coordinates")) {
            if (client.hitResult instanceof net.minecraft.world.phys.EntityHitResult targetHit) {
                var targetEntity = targetHit.getEntity();
                if (AstraModules.isEnabled("Target Distance")) {
                    drawLine(graphics, client, x, y, 150, String.format("Target distance  %.1f m",
                            targetEntity.distanceTo(client.player)));
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Target Coordinates")) {
                    drawLine(graphics, client, x, y, 190, "Target XYZ  " + targetEntity.blockPosition().toShortString());
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
            } else if (client.hitResult instanceof net.minecraft.world.phys.BlockHitResult targetBlock) {
                if (AstraModules.isEnabled("Target Distance")) {
                    double distance = client.player.distanceTo(targetBlock.getBlockPos().getCenter());
                    drawLine(graphics, client, x, y, 150, String.format("Target distance  %.1f m", distance));
                    y += 24;
                    if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Target Coordinates")) {
                    drawLine(graphics, client, x, y, 190, "Target XYZ  " + targetBlock.getBlockPos().toShortString());
                    y += 24;
                    if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
            }
        }
        boolean targetDetailsEnabled = AstraModules.isEnabled("Target Health") || AstraModules.isEnabled("Target Max Health") || AstraModules.isEnabled("Target Armor") || AstraModules.isEnabled("Target Absorption") || AstraModules.isEnabled("Target Name") || AstraModules.isEnabled("Target Type") || AstraModules.isEnabled("Target Entity ID") || AstraModules.isEnabled("Target Held Item");
        if (targetDetailsEnabled && client.hitResult instanceof net.minecraft.world.phys.EntityHitResult targetHit) {
            var targetEntity = targetHit.getEntity();
            if (AstraModules.isEnabled("Target Name")) {
                drawLine(graphics, client, x, y, 190, "Target  " + targetEntity.getName().getString());
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Target Type")) {
                drawLine(graphics, client, x, y, 190, "Type  " + targetEntity.getType().getDescription().getString());
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (AstraModules.isEnabled("Target Entity ID")) {
                drawLine(graphics, client, x, y, 150, "Entity ID  " + targetEntity.getId());
                y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
            }
            if (targetEntity instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
                if (AstraModules.isEnabled("Target Health")) {
                    drawMeter(graphics, client, x, y, 160, "TARGET HEALTH",
                            livingTarget.getHealth(), livingTarget.getMaxHealth(), 0xFFE45B68);
                    y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Target Max Health")) {
                    drawLine(graphics, client, x, y, 160, "Max health  " + String.format("%.1f", livingTarget.getMaxHealth()));
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Target Armor")) {
                    drawLine(graphics, client, x, y, 150, "Target armor  " + livingTarget.getArmorValue());
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Target Absorption")) {
                    drawLine(graphics, client, x, y, 170, "Absorption  " + String.format("%.1f", livingTarget.getAbsorptionAmount()));
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
                if (AstraModules.isEnabled("Target Held Item")) {
                    ItemStack targetItem = livingTarget.getMainHandItem();
                    String itemName = targetItem.isEmpty() ? "Empty" : targetItem.getHoverName().getString();
                    drawLine(graphics, client, x, y, 200, "Target item  " + itemName);
                    y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
                }
            }
        }

        if (AstraModules.isEnabled("Player Height")) {
            drawMetric(graphics, client, x, y, 150, "ALTITUDE",
                    Integer.toString(client.player.getBlockY()), client.player.getBlockY(), 320.0, ACCENT);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Player Name")) {
            drawLine(graphics, client, x, y, 180, "Player  " + client.player.getName().getString());
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Main Hand Durability")) {
            ItemStack mainHand = client.player.getMainHandItem();
            String remaining = mainHand.isEmpty() || !mainHand.isDamageableItem() ? "N/A"
                    : Integer.toString(mainHand.getMaxDamage() - mainHand.getDamageValue());
            drawLine(graphics, client, x, y, 170, "Main hand durability  " + remaining);
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Offhand Durability")) {
            ItemStack offhand = client.player.getOffhandItem();
            String remaining = offhand.isEmpty() || !offhand.isDamageableItem() ? "N/A"
                    : Integer.toString(offhand.getMaxDamage() - offhand.getDamageValue());
            drawLine(graphics, client, x, y, 170, "Offhand durability  " + remaining);
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Total Armor Durability")) {
            int remaining = 0;
            int maximum = 0;
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack armorPiece = client.player.getItemBySlot(slot);
                if (armorPiece.isDamageableItem()) {
                    remaining += armorPiece.getMaxDamage() - armorPiece.getDamageValue();
                    maximum += armorPiece.getMaxDamage();
                }
            }
            drawMeter(graphics, client, x, y, 160, "ARMOR DURABILITY", remaining,
                    Math.max(1, maximum), 0xFF65D58A);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Player Food Level")) {
            drawMeter(graphics, client, x, y, 150, "FOOD LEVEL",
                    client.player.getFoodData().getFoodLevel(), 20.0, 0xFFE8B64F);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Player Air Supply")) {
            drawMeter(graphics, client, x, y, 150, "AIR SUPPLY",
                    client.player.getAirSupply(), client.player.getMaxAirSupply(), ACCENT);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Player Experience Level")) {
            drawMetric(graphics, client, x, y, 150, "XP LEVEL",
                    Integer.toString(client.player.experienceLevel), client.player.experienceLevel, 100.0, 0xFF70D77C);
            y += 34;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Player Rotation")) {
            drawLine(graphics, client, x, y, 170, String.format("Rotation  %.1f / %.1f",
                    client.player.getYRot(), client.player.getXRot()));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Player Ground Status")) {
            drawLine(graphics, client, x, y, 150, "Grounded  " + (client.player.onGround() ? "Yes" : "No"));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Player Sneaking Status")) {
            drawLine(graphics, client, x, y, 150, "Sneaking  " + (client.player.isShiftKeyDown() ? "Yes" : "No"));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }

        if (AstraModules.isEnabled("Chunk Updates")) {
            drawLine(graphics, client, x, y, 190, "Chunk loads/unloads  " + chunkUpdates);
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
        if (AstraModules.isEnabled("Spawnable Light")) {
            int light = client.level.getBrightness(LightLayer.BLOCK, client.player.blockPosition());
            drawLine(graphics, client, x, y, 190, "Block light  " + light
                    + (light == 0 ? "  (hostile mobs can spawn)" : ""));
            y += 24;
            if (y + 46 > layoutBottom) { y = 8; x += 250; }
        }
    }


    private static void drawPanel(GuiGraphicsExtractor graphics, Minecraft client, int x, int y,
            int width, String title) {
        if (y < -28 || y > client.getWindow().getGuiScaledHeight() - 30 || x < 0
                || x + width > hudContentRight) return;
        graphics.fill(x, y, x + width, y + 30, 0xD90B0E14);
        graphics.fill(x, y, x + 2, y + 30, ACCENT);
        graphics.text(client.font, title, x + 7, y + 5, 0xFFB8C7DF);
    }

    private static void drawMetric(GuiGraphicsExtractor graphics, Minecraft client, int x, int y,
            int width, String title, String value, double amount, double max, int color) {
        if (x < 0 || x + width > hudContentRight) return;
        drawPanel(graphics, client, x, y, width, title);
        graphics.text(client.font, value, x + 7, y + 15, WHITE);
        graphics.fill(x + width - 7, y + 17, x + width - 4, y + 25, 0xFF343A45);
        int h = (int)Math.round(8.0 * Math.max(0.0, Math.min(1.0, amount / Math.max(1.0, max))));
        if (h > 0) graphics.fill(x + width - 7, y + 25 - h, x + width - 4, y + 25, color);
    }

    private static void drawMeter(GuiGraphicsExtractor graphics, Minecraft client, int x, int y,
            int width, String title, double value, double max, int color) {
        if (x < 0 || x + width > hudContentRight) return;
        drawPanel(graphics, client, x, y, width, title + "  " + String.format("%.1f / %.0f", value, max));
        graphics.fill(x + 7, y + 19, x + width - 7, y + 25, 0xFF343A45);
        int fill = (int)Math.round((width - 14) * Math.max(0.0, Math.min(1.0, value / Math.max(1.0, max))));
        if (fill > 0) graphics.fill(x + 7, y + 19, x + 7 + fill, y + 25, color);
    }


    private static void drawGraph(GuiGraphicsExtractor graphics, int x, int y, String label, int[] samples, int width) {
        if (y < -46 || y > Minecraft.getInstance().getWindow().getGuiScaledHeight() - 46
                || x < 0 || x + width > hudContentRight) return;
        graphics.fill(x, y, x + width, y + 46, BACKGROUND);
        graphics.text(Minecraft.getInstance().font, label, x + 5, y + 4, WHITE);
        int max = 1;
        for (int value : samples) max = Math.max(max, value);
        int barWidth = Math.max(2, (width - 10) / samples.length);
        for (int i = 0; i < samples.length; i++) {
            int sample = samples[(graphIndex + i) % samples.length];
            int height = Math.max(1, sample * 30 / max);
            int bx = x + 5 + i * barWidth;
            graphics.fill(bx, y + 40 - height, bx + Math.max(1, barWidth - 1), y + 40, ACCENT);
        }
    }

    private static void drawLine(GuiGraphicsExtractor graphics, Minecraft client, int x, int y, int width, String text) {
        if (y < -20 || y > client.getWindow().getGuiScaledHeight() - 20
                || x < 0 || x + width > hudContentRight) return;
        graphics.fill(x, y, x + width, y + 20, BACKGROUND);
        graphics.fill(x, y, x + 2, y + 20, ACCENT);
        graphics.text(client.font, text, x + 7, y + 6, WHITE);
    }

    private static void drawKey(GuiGraphicsExtractor graphics, Minecraft client, int x, int y,
            String key, boolean pressed) {
        drawKey(graphics, client, x, y, key, pressed, 22);
    }

    private static void drawKey(GuiGraphicsExtractor graphics, Minecraft client, int x, int y,
            String key, boolean pressed, int width) {
        int color = pressed ? 0xFF789BD0 : 0xCC171B22;
        graphics.fill(x, y, x + width, y + 20, 0xFF080A0F);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 19, color);
        int textWidth = client.font.width(key);
        graphics.text(client.font, key, x + Math.max(2, (width - textWidth) / 2), y + 6, WHITE);
    }
}
