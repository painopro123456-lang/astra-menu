# Astra Menu

A client-side Fabric mod for Minecraft **26.2**.

Astra's visual direction is deliberately classic: familiar Minecraft-style HUD elements, crisp typography, compact panels and clear spacing. No futuristic glass dashboards or oversized custom HUD concepts.

## Current foundation

- Right Shift opens the initial menu shell.
- Client-only Fabric project targeting Minecraft 26.2 / Java 25.
- Planned traditional HUD modules: coordinates, keystrokes, armor durability, FPS, CPS, direction and status effects.
- Single distributable `.jar` is the intended release format.

## Build

Use JDK 25 and run `./gradlew build` (Windows: `gradlew.bat build`). The mod jar is produced in `build/libs/`.

This is the first project foundation, not a finished release. Module toggles, configuration persistence, live HUD rendering and in-game testing are still to be implemented.
