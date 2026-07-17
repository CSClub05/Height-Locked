# Height-Locked Mining

A client-side Fabric mod for Minecraft Java Edition 1.21.4.

## Behavior

- The mod starts **disabled**.
- Press **X + ]** together to toggle it.
- While enabled, mining is allowed only on two horizontal block layers:
  1. the y layer directly in front of the player
  2. the layer immediately below it, representing the lower body.
- Attempts to mine the floor or any other Y layer are canceled.
- The two permitted layers are recalculated from the player's current eye position for every mining attempt.
- Toggling the mode cancels any currently progressing block break.

The `]` part of the shortcut is available under **Options → Controls → Key Binds → Height-Locked Mining**. `X` is the fixed modifier for this mod.

## Requirements

- Minecraft Java Edition 1.21.4
- Fabric Loader 0.16.10 or newer compatible release
- Fabric API for 1.21.4
- Java 21

The mod is client-only; it does not need to be installed on a multiplayer server.

## Build

macOS/Linux:

```bash
./gradlew build
```

Windows:

```bat
gradlew.bat build
```

The built mod will be placed in:

```text
build/libs/heightlock-1.0.0.jar
```
