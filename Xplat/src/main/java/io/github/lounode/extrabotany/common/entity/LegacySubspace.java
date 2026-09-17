package io.github.lounode.extrabotany.common.entity;

import io.github.lounode.extrabotany.common.handler.DamageHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;

public final class LegacySubspace extends LegacyOwnedEntity {
    public static final EntityType<LegacySubspace> TYPE = EntityType.Builder.<LegacySubspace>of(LegacySubspace::new, MobCategory.MISC)
            .sized(.1F, .1F).clientTrackingRange(8).updateInterval(2).build("extrabotany:subspace");
    private static final EntityDataAccessor<Integer> LIVE_TICKS = SynchedEntityData.defineId(LegacySubspace.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DELAY = SynchedEntityData.defineId(LegacySubspace.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(LegacySubspace.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ROTATION = SynchedEntityData.defineId(LegacySubspace.class, EntityDataSerializers.FLOAT);
    private int interval = 10, count, mode;
    public LegacySubspace(EntityType<? extends LegacySubspace> type, Level level) { super(type, level); }
    public void configure(int mode, int life, int delay, int interval, float size, float rotation) {
        this.mode = mode; this.interval = Math.max(1, interval);
        entityData.set(LIVE_TICKS, life); entityData.set(DELAY, delay); entityData.set(SIZE, size); entityData.set(ROTATION, rotation);
    }
    public int liveTicks() { return entityData.get(LIVE_TICKS); }
    public int delay() { return entityData.get(DELAY); }
    public float visualSize() { return entityData.get(SIZE); }
    public float rotation() { return entityData.get(ROTATION); }
    public float renderScale(float partialTick) {
        float age = tickCount + partialTick;
        return age < liveTicks() ? Math.min(visualSize(), Math.max(0, (age - delay()) / 10))
                : Math.max(0, visualSize() - (age - liveTicks()) / 5);
    }
    @Override public void tick() {
        setDeltaMovement(Vec3.ZERO); super.tick();
        if (level().isClientSide()) return;
        int delay = delay(), liveTicks = liveTicks();
        if (!(getOwner() instanceof LivingEntity owner) || !owner.isAlive()) { discard(); return; }
        if (tickCount < delay) return;
        if (tickCount > liveTicks + delay) { discard(); return; }
        if (mode == 1 && tickCount > delay + 8 && count < 1) { spawn(owner, owner.getLookAngle(), 2.45F, 100); count++; }
        else if ((mode == 0 || mode == 2) && tickCount % interval == 0 && count < (mode == 2 ? 6 : 5)
                && tickCount > delay + 5 && tickCount < liveTicks - delay - 10) {
            net.minecraft.world.entity.player.Player aimingPlayer = owner instanceof net.minecraft.world.entity.player.Player player ? player
                    : owner instanceof io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher boss
                    ? boss.getPlayersAround().stream().findFirst().orElse(null) : null;
            if (aimingPlayer == null) { discard(); return; }
            level().addFreshEntity(LegacySubspaceBurst.create(owner, aimingPlayer, position(), mode == 2)); count++;
        }
    }
    private void spawn(LivingEntity owner, Vec3 direction, float speed, int life) {
        if (direction.lengthSqr() == 0) direction = owner.getLookAngle();
        var spear = new LegacySubspaceSpear(LegacySubspaceSpear.TYPE, level()); spear.setOwner(owner);
        spear.configure(owner instanceof io.github.lounode.extrabotany.common.entity.gaia.LegacyVoidHerrscher ? 14 : 12, life);
        spear.setPos(getX(), getY() - .75, getZ()); spear.shoot(direction.x, direction.y, direction.z, speed, 1);
        level().addFreshEntity(spear);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIVE_TICKS, 0); builder.define(DELAY, 0); builder.define(SIZE, 0F); builder.define(ROTATION, 0F);
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putInt("LiveTicks", liveTicks()); tag.putInt("Delay", delay()); tag.putInt("Interval", interval);
        tag.putInt("Count", count); tag.putInt("Type", mode); tag.putFloat("Size", visualSize()); tag.putFloat("Rotation", rotation());
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        configure(tag.getInt("Type"), tag.getInt("LiveTicks"), tag.getInt("Delay"), tag.getInt("Interval"), tag.getFloat("Size"), tag.getFloat("Rotation"));
        count = tag.getInt("Count");
    }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean isPushable() { return false; }
    @Override public boolean isPushedByFluid() { return false; }
}
