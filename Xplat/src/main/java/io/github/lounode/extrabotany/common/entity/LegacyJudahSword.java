package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Sakura's moving star edge is rendered by its original particles, without an invented mesh. */
public final class LegacyJudahSword extends Entity {
    public static final EntityType<LegacyJudahSword> TYPE = EntityType.Builder.<LegacyJudahSword>of(LegacyJudahSword::new, MobCategory.MISC)
            .sized(.35F, .35F).clientTrackingRange(8).updateInterval(2).build("extrabotany:judah_sword");
    private Entity owner;
    private static final EntityDataAccessor<org.joml.Vector3f> STEP = SynchedEntityData.defineId(LegacyJudahSword.class, EntityDataSerializers.VECTOR3);
    private Vec3 start = Vec3.ZERO, end = Vec3.ZERO;
    private float damage = 6;
    public LegacyJudahSword(EntityType<? extends LegacyJudahSword> type, Level level) { super(type, level); }
    public void configure(Entity owner, Vec3 start, Vec3 end, float damage) { this.owner = owner; this.start = start; this.end = end; this.damage = damage; setPos(start); syncStep(); }
    private void syncStep() { entityData.set(STEP, end.subtract(start).normalize().scale(.75).toVector3f()); }
    @Override public void tick() {
        var step = entityData.get(STEP);
        setPos(position().add(step.x, step.y, step.z));
        if (level().isClientSide()) level().addParticle(vazkii.botania.client.fx.SparkleParticleData.sparkle(1.8F, 1, .7F, .7F, 25), getX(), getY(), getZ(), 0, 0, 0);
        if (!level().isClientSide()) {
            hit(getBoundingBox().inflate(.75), false);
            if (position().distanceToSqr(end) <= 2 || tickCount > 80) { hit(new AABB(start, end).inflate(1.8), true); discard(); }
        }
        super.tick();
    }
    private void hit(AABB area, boolean line) {
        for (var target : level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (target instanceof Player || !DamageHandler.INSTANCE.checkPassable(target, owner)) continue;
            // The star's diagonal may cross a large bounding rectangle: only its actual edge deals the finisher.
            if (line && !target.getBoundingBox().inflate(1.8).contains(start)
                    && target.getBoundingBox().inflate(1.8).clip(start, end).isEmpty()) continue;
            var source = owner instanceof Player player ? damageSources().playerAttack(player)
                    : owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().magic();
            target.hurt(source, damage * (line ? .9F : .4F));
            if (line && target.isAlive() && damage > 0) {
                if (target.getHealth() <= damage * .3F) target.hurt(damageSources().magic(), Float.MAX_VALUE);
                else { target.setHealth(Math.max(1, target.getHealth() - damage * .3F)); target.hurt(damageSources().magic(), .01F); }
            }
            target.igniteForSeconds(5);
        }
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(STEP, new org.joml.Vector3f()); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putDouble("StartX", start.x); tag.putDouble("StartY", start.y); tag.putDouble("StartZ", start.z);
        tag.putDouble("EndX", end.x); tag.putDouble("EndY", end.y); tag.putDouble("EndZ", end.z); tag.putFloat("Damage", damage);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        start = new Vec3(tag.getDouble("StartX"), tag.getDouble("StartY"), tag.getDouble("StartZ"));
        end = new Vec3(tag.getDouble("EndX"), tag.getDouble("EndY"), tag.getDouble("EndZ")); damage = tag.getFloat("Damage");
        syncStep();
    }
}
