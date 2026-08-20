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
- Java 21 or newer

The is a client-side mod.
