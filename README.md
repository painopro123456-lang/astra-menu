# DFU Fast

A small client-side Fabric startup diagnostic for Minecraft **26.2**.

## What it does

When Fabric begins client initialization, DFU Fast logs how many milliseconds have elapsed since the Java virtual machine started.

This gives us a repeatable checkpoint in the startup log. It does not preload DFU classes, alter DataFixerUpper, skip data conversion, modify worlds, or compete with Minecraft's startup thread.

## Performance note

This build is diagnostic-only. It does **not** claim to make Minecraft launch faster. The checkpoint helps identify whether time is being spent before Fabric client initialization; the full launch bottleneck still needs to be measured using launcher timing and the Minecraft log.

## Compatibility

- Minecraft Java Edition 26.2
- Fabric Loader 0.19.3+
- Java 25+
- Client-side only
- No Fabric API dependency

## Safety

This mod only reads JVM uptime and writes a log message. It does not edit worlds, change DataFixer rules, or alter vanilla startup behavior.
