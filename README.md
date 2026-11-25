# DFU Fast

A small client-side Fabric mod for Minecraft **26.2** that skips the eager DataFixerUpper (DFU) optimization pass during startup.

## What it changes

DFU Fast injects at the start of Minecraft's `SharedConstants.enableDataFixerOptimization()` method and cancels that one startup call. It uses a required Mixin injection (`require = 1`), so if the target method is missing or the hook cannot be applied, startup fails loudly instead of silently shipping an inactive optimization.

DFU itself is not removed, and the mod does not replace Minecraft's data conversion rules. This changes when the optimization pass runs; it does not promise that every possible world conversion will be prepared later in exactly the same way on every game version. Back up worlds before testing.

## Performance expectations

This targets only the DFU optimization call during startup. It does not speed up Fabric mod discovery, resource loading, Java startup, or other launch work.

Mojang improved DFU initialization in modern Minecraft versions, so the benefit may be small or even unnoticeable on 26.2. No specific time saving is guaranteed. Compare several launches with and without the mod, using the same mod list and launcher.

## Compatibility

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3+
- Java 25+
- Client-side only
- No Fabric API dependency

## Safety and troubleshooting

- Back up important worlds before testing.
- Test old worlds only on copies.
- If Minecraft crashes on startup, remove DFU Fast and share the crash log.
- If startup is not faster, remove the mod; do not expect it to improve chunk loading, FPS, or network ping.
