# Height-Locked Mining

A client-side Fabric mod for Minecraft Java Edition 1.21.11.

## Behavior

- The mod starts **disabled**.
- Press **X + ]** together to toggle it.
- While enabled, mining is allowed only on two horizontal block layers:
  1. the integer Y layer containing the player's eyes; and
  2. the layer immediately below it, representing the lower body.
- Attempts to mine the floor or any other Y layer are canceled.
- The two permitted layers are recalculated from the player's current eye position for every mining attempt.
- Toggling the mode cancels any currently progressing block break.

The `]` part of the shortcut is available under **Options → Controls → Key Binds → Height-Locked Mining**. `X` remains the fixed modifier.

## Requirements

- Minecraft Java Edition 1.21.11
- Fabric Loader 0.19.3 or newer compatible release
- Fabric API 0.141.6+1.21.11 or another compatible 1.21.11 release
- Java 21 or newer for Gradle; the mod targets Java 21 bytecode

The mod is marked client-only; it does not need to be installed on a multiplayer server.

## Build

macOS/Linux:

```bash
./gradlew clean build
```

Windows:

```powershell
.\gradlew.bat clean build
```

The built mod will be placed in:

```text
build/libs/heightlock-1.2.0.jar
```

## Install

1. Install Fabric Loader for Minecraft 1.21.11.
2. Install Fabric API for Minecraft 1.21.11.
3. Copy `heightlock-1.2.0.jar` into the Minecraft `mods` folder.
4. Start the Fabric 1.21.11 profile.

## 1.21.11 port notes

This release updates the project to Minecraft 1.21.11, Yarn `1.21.11+build.6`, Fabric Loader `0.19.3`, Fabric API `0.141.6+1.21.11`, Fabric Loom `1.14.10`, and the Gradle `9.2.1` distribution.

The client input and mining APIs used by the mod remain compatible in 1.21.11. The mining interception points are still `attackBlock`, `updateBlockBreakingProgress`, and `breakBlock` in `ClientPlayerInteractionManager`. The keybinding continues to use `KeyBinding.Category`, and the X modifier check continues to use `InputUtil.isKeyPressed(Window, int)`.
