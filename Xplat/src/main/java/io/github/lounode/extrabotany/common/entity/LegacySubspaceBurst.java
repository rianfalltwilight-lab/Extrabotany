package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.legacy.LegacyRelicSword;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.BotaniaDamageTypes;

/** The two golden Excaliber burst branches used by master EntitySubspace. */
public final class LegacySubspaceBurst {
    private LegacySubspaceBurst() {}
    public static ManaBurstEntity create(LivingEntity owner, Player aimingPlayer, Vec3 position, boolean evil) {
        var burst = new ManaBurstEntity(aimingPlayer);
        burst.setOwner(owner); burst.setPos(position);
        burst.setColor(evil ? 0xFFD700 : 0xFFAF00);
        burst.setMana(160); burst.setStartingMana(160); burst.setMinManaLoss(40);
        burst.setManaLossPerTick(4); burst.setGravity(0);
        burst.setDeltaMovement(evil ? Vec3.directionFromRotation(owner.getXRot() + 15, owner.getYRot()) : burst.getDeltaMovement().scale(9));
        burst.setSourceLens(new ItemStack(LegacyRelicSword.ITEMS.get("spear_of_subspace")));
        return burst;
    }
    public static void update(ManaBurst burst) {
        var entity = (ManaBurstEntity) burst.entity();
        if (entity.level().isClientSide()) return;
        var owner = entity.getOwner();
        if (!(owner instanceof LivingEntity living) || !living.isAlive()) { entity.discard(); return; }
        boolean evil = burst.getColor() == 0xFFD700;
        var bounds = new AABB(entity.position(), new Vec3(entity.xOld, entity.yOld, entity.zOld)).inflate(1.3);
        for (var target : entity.level().getEntitiesOfClass(LivingEntity.class, bounds)) {
            if (evil && target instanceof LegacyVoidHerrscher || !DamageHandler.INSTANCE.checkPassable(target, owner)) continue;
            boolean boss = target.getType().is(Tags.EntityTypes.BOSSES)
                    || target instanceof io.github.lounode.extrabotany.common.entity.gaia.Gaia;
            if (entity.tickCount % 10 == 0) LegacySwordProjectile.trueMagicDamage(target, owner, evil ? 1.4F : .22F);
            if (target.invulnerableTime != 0 || burst.isFake()) continue;
            target.hurt(BotaniaDamageTypes.Sources.relicDamage(entity.level().registryAccess()), evil ? 10 : boss ? 22 : 16);
            LegacySwordProjectile.trueMagicDamage(target, owner, evil ? 4 : boss ? 9 : 7);
            target.invulnerableTime = 10;
            burst.setFake(true);
        }
    }
}
