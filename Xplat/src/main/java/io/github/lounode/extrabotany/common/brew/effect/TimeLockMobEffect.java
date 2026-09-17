package io.github.lounode.extrabotany.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class TimeLockMobEffect extends MobEffect {
    public TimeLockMobEffect() { super(MobEffectCategory.HARMFUL, 0xFFD700); }
    @Override public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) { return true; }
    @Override public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getDeltaMovement().y < 0) entity.setDeltaMovement(entity.getDeltaMovement().scale(.03));
        return true;
    }
}
