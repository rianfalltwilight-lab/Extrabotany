package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.*;
import io.github.lounode.extrabotany.common.handler.DamageHandler;
import io.github.lounode.extrabotany.common.item.legacy.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.common.entity.ManaBurstEntity;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class UpstreamParityGameTests {
    private static Player player(GameTestHelper h, GameType mode) {
        var player = h.makeMockPlayer(mode); player.setPos(h.absoluteVec(new Vec3(3, 8, 3))); player.setNoGravity(true); return player;
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void spearFanStaysPerpendicularForEveryView(GameTestHelper h) {
        for (int yaw = -180; yaw <= 180; yaw += 15) {
            var look = Vec3.directionFromRotation(0, yaw);
            var distinct = new java.util.HashSet<String>();
            for (int i = 0; i < 24; i++) {
                var offset = LegacySubspaceSpearItem.domainOffset(look, i);
                h.assertTrue(Double.isFinite(offset.length()) && Math.abs(offset.length() - (5 + i / 8 * 3.5)) < 1E-5,
                        "Fan radius/NaN mismatch at yaw=" + yaw + " index=" + i);
                h.assertTrue(Math.abs(offset.dot(look)) < 1E-5 && offset.y >= 0, "Fan rotated around world X instead of look at yaw=" + yaw);
                distinct.add(String.format(java.util.Locale.ROOT,"%.3f,%.3f,%.3f",offset.x,offset.y,offset.z));
            }
            h.assertTrue(distinct.size() == 24, "Portals collapse onto each other at yaw=" + yaw);
        }
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void portalVisualStateSurvivesSaveAndSync(GameTestHelper h) {
        var portal = LegacySubspace.TYPE.create(h.getLevel()); portal.configure(0,120,23,13,1.75F,57);
        var loaded = LegacySubspace.TYPE.create(h.getLevel()); loaded.load(portal.saveWithoutId(new CompoundTag()));
        var synced = LegacySubspace.TYPE.create(h.getLevel()); synced.getEntityData().assignValues(portal.getEntityData().getNonDefaultValues());
        for (var copy : java.util.List.of(loaded,synced)) {
            h.assertTrue(copy.liveTicks()==120 && copy.delay()==23 && copy.visualSize()==1.75F && copy.rotation()==57,"Client/render save fields missing");
            copy.tickCount=20; h.assertTrue(copy.renderScale(0)==0,"Portal visible before delay");
            copy.tickCount=48; h.assertTrue(copy.renderScale(0)==1.75F,"Portal did not grow to original size");
            copy.tickCount=130; h.assertTrue(copy.renderScale(0)==0,"Portal never fades");
        }
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void domainFiresFiveGoldenBurstsRatherThanSpears(GameTestHelper h) {
        var owner=player(h,GameType.SURVIVAL);owner.setYRot(35);owner.setXRot(-20);
        var portal=LegacySubspace.TYPE.create(h.getLevel());portal.setOwner(owner);portal.setPos(owner.position().add(0,4,0));portal.configure(0,120,15,10,1.5F,145);
        for(int i=0;i<100;i++)h.getLevel().tickNonPassenger(portal);
        var bounds=portal.getBoundingBox().inflate(4);
        var bursts=h.getLevel().getEntitiesOfClass(ManaBurstEntity.class,bounds,e->e.getOwner()==owner);
        h.assertTrue(bursts.size()==5,"Expected five original burst volleys, got "+bursts.size());
        h.assertTrue(h.getLevel().getEntitiesOfClass(LegacySubspaceSpear.class,bounds,e->e.getOwner()==owner).isEmpty(),"Charged domain incorrectly fires normal spears");
        for(var burst:bursts)h.assertTrue(burst.getColor()==0xFFAF00 && burst.getMana()==160 && Math.abs(burst.getDeltaMovement().length()-1.8)<1E-4,"Original burst color, speed or mana differs");
        bursts.forEach(net.minecraft.world.entity.Entity::discard);h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void ignitionLaunchesAndEffectControlsDescent(GameTestHelper h) {
        var owner=player(h,GameType.SURVIVAL);owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(LegacyFlamescionItem.INSTANCE));
        owner.setOnGround(true);owner.setShiftKeyDown(true);LegacyFlamescionItem.INSTANCE.use(h.getLevel(),owner,InteractionHand.MAIN_HAND);
        h.assertTrue(owner.getDeltaMovement().y==1 && owner.hasEffect(ExtraBotanyMobEffects.INCANDESCENCE),"Ground ignition did not launch its owner");
        owner.setOnGround(false);owner.setDeltaMovement(.4,-1,.2);
        h.assertTrue(ExtraBotanyMobEffects.INCANDESCENCE.value().applyEffectTick(owner,0),"Airborne state removed");
        h.assertTrue(Math.abs(owner.getDeltaMovement().y+.05)<1E-8 && owner.getDeltaMovement().x==.4,"Original ignition fall multiplier missing");
        owner.setOnGround(true);h.assertTrue(!ExtraBotanyMobEffects.INCANDESCENCE.value().applyEffectTick(owner,0),"Ignition survives landing");
        h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void timeLockSlowsFallingMotionWithoutReplacingMoveAttribute(GameTestHelper h) {
        var mob=EntityType.HUSK.create(h.getLevel());mob.setDeltaMovement(.4,-1,.2);
        ExtraBotanyMobEffects.TIMELOCK.value().applyEffectTick(mob,0);
        h.assertTrue(mob.getDeltaMovement().distanceTo(new Vec3(.012,-.03,.006))<1E-8,"Time lock fall-motion semantics missing");
        mob.setDeltaMovement(0,1,0);ExtraBotanyMobEffects.TIMELOCK.value().applyEffectTick(mob,0);
        h.assertTrue(mob.getDeltaMovement().y==1,"Time lock destroys initial upward launch");h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void creativeTargetsCannotLoseHealthToLegacyTrueDamage(GameTestHelper h) {
        var attacker=player(h,GameType.SURVIVAL);var target=player(h,GameType.CREATIVE);float health=target.getHealth();
        h.assertTrue(!DamageHandler.INSTANCE.checkPassable(target,attacker),"Creative target admitted to direct health damage");
        LegacySwordProjectile.trueMagicDamage(target,attacker,10);
        h.assertTrue(target.getHealth()==health,"Creative player lost health without a hurt event");h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void flamescionDamageRetainsArmorBypass(GameTestHelper h) {
        var owner=player(h,GameType.SURVIVAL);var target=EntityType.HUSK.create(h.getLevel());target.setPos(owner.position().add(2,0,0));target.setNoAi(true);
        target.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.NETHERITE_CHESTPLATE));target.getAttribute(Attributes.ARMOR).setBaseValue(30);
        h.getLevel().addFreshEntity(target);
        var source=LegacyFlameArea.create(LegacyFlameArea.Kind.ULT,owner,owner.position());float health=target.getHealth();
        LegacyFlameArea.damageAround(source,owner,4,12);
        h.assertTrue(Math.abs(health-target.getHealth()-12)<.001,"Original absolute flame damage was reduced by armor");target.discard();h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void flamescionOverloadBelongsToPlayerAndCoolsWhileUnequipped(GameTestHelper h) {
        var owner=player(h,GameType.SURVIVAL);var original=new ItemStack(LegacyFlamescionItem.INSTANCE);
        LegacyFlamescionItem.energy(original,600);LegacyFlamescionItem.overloaded(original,true);owner.setItemInHand(InteractionHand.MAIN_HAND,original);
        LegacyFlamescionItem.tickPlayer(owner);
        var replacement=new ItemStack(LegacyFlamescionItem.INSTANCE);owner.setItemInHand(InteractionHand.MAIN_HAND,replacement);
        h.assertTrue(LegacyFlamescionItem.overloaded(owner),"Swapping weapons bypasses player overload");
        LegacyFlamescionItem.tickPlayer(owner);h.assertTrue(LegacyFlamescionItem.energy(replacement)==594,"Replacement weapon did not receive player state");
        var restored=player(h,GameType.SURVIVAL);restored.load(owner.saveWithoutId(new CompoundTag()));
        h.assertTrue(LegacyFlamescionItem.overloaded(restored),"Player overload lost on save/reload");
        owner.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
        for(int i=0;i<199;i++)LegacyFlamescionItem.tickPlayer(owner);
        h.assertTrue(!LegacyFlamescionItem.overloaded(owner),"Overload recovery stops when weapon is put away");
        LegacyFlamescionItem.cloneState(restored,owner,true);
        h.assertTrue(!LegacyFlamescionItem.overloaded(owner),"Death clone retained overload against upstream reset behavior");h.succeed();
    }
    @GameTest(template="empty", timeoutTicks=30)
    public static void judahStarMotionSyncsAndFinisherUsesLineIntersection(GameTestHelper h) {
        var owner=player(h,GameType.SURVIVAL);var start=owner.position().add(0,40,0);var end=start.add(10,0,10);
        var sword=new LegacyJudahSword(LegacyJudahSword.TYPE,h.getLevel());sword.configure(owner,start,end,6);
        var mirror=new LegacyJudahSword(LegacyJudahSword.TYPE,h.getLevel());mirror.setPos(start);
        mirror.getEntityData().assignValues(sword.getEntityData().getNonDefaultValues());mirror.tick();
        h.assertTrue(Math.abs(mirror.position().distanceTo(start)-.75)<1E-6,"Client star edge does not advance from synced movement");
        var onLine=EntityType.HUSK.create(h.getLevel());onLine.setPos(start.add(5,0,5));onLine.setNoAi(true);h.getLevel().addFreshEntity(onLine);
        var offLine=EntityType.HUSK.create(h.getLevel());offLine.setPos(start.add(1,0,9));offLine.setNoAi(true);h.getLevel().addFreshEntity(offLine);
        float onHealth=onLine.getHealth(),offHealth=offLine.getHealth();sword.tickCount=81;sword.tick();
        h.assertTrue(onLine.getHealth()<onHealth && offLine.getHealth()==offHealth,"Star finisher damages the whole diagonal bounding rectangle");
        onLine.discard();offLine.discard();mirror.discard();h.succeed();
    }
    @net.minecraft.gametest.framework.GameTest(template="empty")
    public static void effect_holders_survive_serialization(net.minecraft.gametest.framework.GameTestHelper h) {
        var effects=java.util.List.of(ExtraBotanyMobEffects.ETERNITY,ExtraBotanyMobEffects.INCANDESCENCE,
                ExtraBotanyMobEffects.FLAMESCION,ExtraBotanyMobEffects.WITCH_CURSE,ExtraBotanyMobEffects.TIMELOCK,
                ExtraBotanyMobEffects.IMMOBILIZE,ExtraBotanyMobEffects.LINK,ExtraBotanyMobEffects.HEAL_REVERSE,
                ExtraBotanyMobEffects.DISCOUNT,ExtraBotanyMobEffects.WARM,ExtraBotanyMobEffects.THIRROR);
        for(var effect:effects) {
            var canonical=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(effect.unwrapKey().orElseThrow());
            h.assertTrue(effect==canonical,"Effect proxy differs from network holder: "+effect.unwrapKey());
            var instance=new net.minecraft.world.effect.MobEffectInstance(effect,80);
            var encoded=net.minecraft.world.effect.MobEffectInstance.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,instance).getOrThrow();
            var decoded=net.minecraft.world.effect.MobEffectInstance.CODEC.parse(net.minecraft.nbt.NbtOps.INSTANCE,encoded).getOrThrow();
            var mob=net.minecraft.world.entity.EntityType.VILLAGER.create(h.getLevel());
            mob.addEffect(decoded);h.assertTrue(mob.hasEffect(effect),"Saved effect cannot be queried after decode");
            h.assertTrue(mob.removeEffect(effect),"Decoded effect cannot be removed through its registered constant");
        }
        h.succeed();
    }
}
