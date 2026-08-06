package dev.heightlock;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Client entry point for Height-Locked Mining.
 */
public final class HeightLockClient implements ClientModInitializer {
    public static final String MOD_ID = "heightlock";

    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(
            Identifier.of(MOD_ID, "controls")
    );

    private static KeyBinding toggleKey;
    private static boolean enabled;
    private static boolean chordWasDown;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.heightlock.toggle",
                InputUtil.Type.KEYSYM,
                InputUtil.GLFW_KEY_RIGHT_BRACKET,
                KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(HeightLockClient::onEndClientTick);
    }

    private static void onEndClientTick(MinecraftClient client) {
        boolean modifierDown = InputUtil.isKeyPressed(
                client.getWindow(),
                InputUtil.GLFW_KEY_X
        );
        boolean chordDown = modifierDown && toggleKey.isPressed();

        // Edge detection prevents one held chord from toggling every client tick.
        if (client.currentScreen == null && chordDown && !chordWasDown) {
            toggle(client);
        }

        chordWasDown = chordDown;
    }

    private static void toggle(MinecraftClient client) {
        enabled = !enabled;

        // Stop any in-progress crack animation immediately when the mode changes.
        if (client.interactionManager != null) {
            client.interactionManager.cancelBlockBreaking();
        }

        if (client.player != null) {
            client.player.sendMessage(
                    Text.translatable(enabled
                                    ? "message.heightlock.enabled"
                                    : "message.heightlock.disabled")
                            .formatted(enabled ? Formatting.GREEN : Formatting.RED),
                    true
            );
        }
    }

    /**
     * Returns whether a block may be mined while the lock is active.
     *
     * <p>The permitted layers move with the player:</p>
     * <ul>
     *     <li>the integer Y layer containing the player's eyes, and</li>
     *     <li>the layer immediately below it (the player's lower-body layer).</li>
     * </ul>
     */
    public static boolean isMiningAllowed(BlockPos pos) {
        if (!enabled) {
            return true;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return true;
        }

        int headY = MathHelper.floor(client.player.getEyeY());
        int lowerBodyY = headY - 1;
        int targetY = pos.getY();

        return targetY == headY || targetY == lowerBodyY;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
