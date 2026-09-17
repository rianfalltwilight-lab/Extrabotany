package io.github.lounode.extrabotany.mixin.client;

import io.github.lounode.extrabotany.common.item.legacy.LegacyAccessories;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Repeat normal attacks only after vanilla has consumed the initial click. */
@Mixin(Minecraft.class)
public abstract class PowerGloveInputMixin {
    @Shadow private boolean startAttack() { throw new AssertionError(); }

    @Inject(method = "handleKeybinds", at = @At("TAIL"))
    private void repeatAttack(CallbackInfo ci) {
        var mc = (Minecraft) (Object) this;
        var player = mc.player;
        if (player == null || mc.gameMode == null || mc.screen != null || mc.isPaused()
                || !mc.isWindowActive() || player.isSpectator() || player.isUsingItem()
                || !mc.options.keyAttack.isDown() || player.getAttackStrengthScale(0) < 1
                || mc.gameMode.isDestroying() || mc.hitResult == null
                || mc.hitResult.getType() == HitResult.Type.BLOCK) return;
        if (LegacyAccessories.worn("power_glove", player)) startAttack();
    }
}
