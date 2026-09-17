package io.github.lounode.extrabotany.common.brew.effect;

import io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Original 1.16 ignition: stay aloft while wielding the weapon; expire on landing/overload. */
public final class IncandescenceMobEffect extends MobEffect {
    public IncandescenceMobEffect() { super(MobEffectCategory.BENEFICIAL, 0xDC143C); }
    @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
    @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        var motion = entity.getDeltaMovement();
        if (motion.y < 0) entity.setDeltaMovement(motion.multiply(1, .05, 1));
        return !(entity instanceof Player player) || LegacyFlamescionItem.mode(player);
    }
}
