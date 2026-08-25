# Height-Locked Mining

A client-side Fabric mod for Minecraft Java Edition 1.21.5.

## Behavior

- The mod starts **disabled**.
- Press **X + ]** together to toggle it.
- While enabled, mining is allowed only on two horizontal block layers:
  1. the integer Y layer containing the player's eyes; and
  2. the layer immediately below it, representing the lower body.
- Attempts to mine the floor or any other Y layer are canceled.
- The two permitted layers are recalculated from the player's current eye position for every mining attempt.
- Toggling the mode cancels any currently progressing block break.

The `]` part of the shortcut is available under **Options → Controls → Key Binds → Height-Locked Mining**. `X` is the fixed modifier in version 1.0.2.

## Requirements

- Minecraft Java Edition 1.21.5
- Fabric Loader 0.16.14 or newer compatible release
- Fabric API for 1.21.5
- Java 21

The mod is marked client-only; it does not need to be installed on a multiplayer server.

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
build/libs/heightlock-1.0.2.jar
```

## Install

1. Install Fabric Loader for Minecraft 1.21.5.
2. Install a compatible Fabric API release.
3. Copy `heightlock-1.0.2.jar` into the Minecraft `mods` folder.
4. Start the Fabric 1.21.5 profile.

## Technical notes

The mixin intercepts `attackBlock`, `updateBlockBreakingProgress`, and `breakBlock` in the client interaction manager. This covers initial clicks, held mining progress, and immediate/creative block breaking.
