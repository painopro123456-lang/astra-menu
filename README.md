# DFU Fast

A small client-side Fabric mod for Minecraft **26.2**.

## What it does

DFU Fast starts a low-priority daemon thread during client initialization and asks the class loader to load a small set of DataFixerUpper-related classes.

It uses non-initializing class loading. It does **not** run static initializers, create a replacement DataFixer, alter world data, skip migrations, or delay vanilla work until later.

## Performance note

This is a conservative startup experiment, not a guaranteed large launch-time boost. It may move some class-loading work away from the main thread, but JVM verification/linking may still happen later, and asset loading, other mods, storage speed, or CPU contention may dominate startup. Compare several cold launches with and without the mod.

## Compatibility

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3+
- Java 25+
- Client-side only
- No Fabric API dependency

## Build

Use JDK 25 and Gradle 9.6.1:

```sh
gradle build
```

The mod jar is written to `build/libs/`.

## Safety

DFU Fast never edits worlds, changes DataFixer rules, disables data conversion, or replaces vanilla's DataFixerUpper implementation.
