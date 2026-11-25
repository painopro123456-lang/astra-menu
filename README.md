# DFU Fast

A small client-side Fabric mod for Minecraft **26.2** that defers eager DataFixerUpper (DFU) migration-rule baking.

## What it changes

DFU Fast replaces Minecraft's `SharedConstants.enableDataFixerOptimizations()` method with a no-op. This prevents that startup call from eagerly baking every historical DFU migration rule.

DFU itself is not removed. Minecraft can still build and use the required migration rules later when they are needed to load or upgrade older data. That deferred work may cause a pause the first time it is needed.

## Performance expectations

This targets one specific part of startup: DFU rule initialization. It does not speed up Fabric mod discovery, resource loading, Java startup, or other launch work.

Mojang significantly improved DFU initialization starting in Minecraft 1.19.4, so the benefit on modern versions can be small and depends on the machine and game build. No specific time saving is guaranteed; compare repeated launches with and without the mod to measure your result.

## Compatibility

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3+
- Java 25+
- Client-side only
- No Fabric API dependency

## Safety and testing

The mod changes when DFU migration rules are prepared, not the conversion rules themselves. Nevertheless, back up important worlds before testing any mod that changes world-data initialization. Test an older world only on a copy and allow any deferred migration work to finish.

If the game crashes on startup, remove DFU Fast and share the crash log so the mixin can be checked against that exact game build.
