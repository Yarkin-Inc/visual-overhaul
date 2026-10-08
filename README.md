# VisualOverhaul
https://www.curseforge.com/minecraft/mc-mods/visual-overhaul

Revamps the visual aspect of certain vanilla blocks.

## Fabric 26.3 development port

This branch builds Visual Overhaul for **Minecraft 26.3**, **Java 25**,
**Fabric Loader 0.19.5+** and **Fabric API 0.161.0+26.3**.
MidnightLib 1.9.3 is included in the mod jar.

Build with `./gradlew build`. The installable jar is
`build/libs/visualoverhaul-fabric-6.0.0+mc26.3-dev.jar`.
Install it with Fabric API in the client's `mods` directory. Install the same
mod and Fabric API on the server for visible furnace and brewing inventories.
Client-only installations retain biome colors, button icons and jukebox sound
recognition; a server without the mod cannot supply hidden block inventories.

The port uses Minecraft's render extraction/submission API, separate block and
item atlases, and per-block inventory synchronization. The original NeoForge,
Quilt and Architectury subprojects are retained as historical sources and are
not included in this Fabric build. Compatibility with other mods has not been
verified.

Run the in-game regression test with `./gradlew runClientGameTest`. On a Linux
host without a display or audio device, use:

```sh
SDL_VIDEODRIVER=offscreen ALSOFT_DRIVERS=null ./gradlew runClientGameTest
```

The test creates a disposable singleplayer world and checks builtin packs,
dynamic models, inventory updates/removal, packet round trips, potion colors,
resource reload, returning to unloaded chunks, and disconnect cleanup.
Screenshots are saved under `build/run/clientGameTest/screenshots/`.
`./gradlew runServer --args='--initSettings --nogui'` checks dedicated-server
bootstrap without starting a world.
