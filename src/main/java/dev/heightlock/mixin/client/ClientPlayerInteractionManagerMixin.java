package dev.heightlock.mixin.client;

import dev.heightlock.HeightLockClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Blocks every normal client mining path outside the two permitted Y layers.
 */
@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Shadow
    public abstract void cancelBlockBreaking();

    @Inject(method = "attackBlock", at = @At("HEAD"), cancellable = true)
    private void heightlock$restrictAttack(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!HeightLockClient.isMiningAllowed(pos)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"), cancellable = true)
    private void heightlock$restrictProgress(
            BlockPos pos,
            Direction direction,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!HeightLockClient.isMiningAllowed(pos)) {
            cancelBlockBreaking();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "breakBlock", at = @At("HEAD"), cancellable = true)
    private void heightlock$restrictBreak(
            BlockPos pos,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!HeightLockClient.isMiningAllowed(pos)) {
            cir.setReturnValue(false);
        }
    }
}
