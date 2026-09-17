package io.github.lounode.extrabotany.common.item.legacy;

import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.common.entity.LegacyFlameArea;
import io.github.lounode.extrabotany.common.entity.LegacyFlameProjectile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class LegacyFlamescionItem extends SwordItem {
    public static final LegacyFlamescionItem INSTANCE = new LegacyFlamescionItem();
    private static final Map<Player, Long> LAST_ATTACK = new WeakHashMap<>();
    private static final String STATE = "extrabotany:flamescion";
    private LegacyFlamescionItem() { super(Tiers.NETHERITE, new Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 5, -1.6F))); }
    public static int energy(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("flamescion_energy"); }
    public static boolean overloaded(ItemStack stack) { return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean("flamescion_overloaded"); }
    public static void energy(ItemStack stack, int amount) { CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt("flamescion_energy", Math.clamp(amount, 0, 600))); }
    public static void overloaded(ItemStack stack, boolean value) { CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean("flamescion_overloaded", value)); }
    private static net.minecraft.nbt.CompoundTag state(Player player) {
        var data = player.getPersistentData();
        if (!data.contains(STATE, net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            // Import existing scex.7 item state once; thereafter overload belongs to the player as upstream does.
            var state = new net.minecraft.nbt.CompoundTag(); var held = player.getMainHandItem();
            state.putInt("energy", held.is(INSTANCE) ? energy(held) : 0);
            state.putBoolean("overloaded", held.is(INSTANCE) && overloaded(held)); data.put(STATE, state);
        }
        return data.getCompound(STATE);
    }
    public static boolean overloaded(Player player) {
        return player.level().isClientSide() ? overloaded(player.getMainHandItem()) : state(player).getBoolean("overloaded");
    }
    private static void projectState(Player player, net.minecraft.nbt.CompoundTag state) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (!stack.is(INSTANCE)) continue;
            int energy = state.getInt("energy"); boolean overloaded = state.getBoolean("overloaded");
            if (energy(stack) != energy) energy(stack, energy);
            if (overloaded(stack) != overloaded) overloaded(stack, overloaded);
        }
    }
    public static void cloneState(Player original, Player replacement, boolean wasDeath) {
        var data = wasDeath ? new net.minecraft.nbt.CompoundTag() : state(original).copy();
        replacement.getPersistentData().put(STATE, data); projectState(replacement, data);
    }
    public static boolean mode(Player player) { return !player.onGround() && player.getMainHandItem().is(INSTANCE)
            && player.hasEffect(ExtraBotanyMobEffects.INCANDESCENCE) && !overloaded(player); }
    public static void tickPlayer(Player player) {
        if (player.level().isClientSide()) return;
        if (!player.getMainHandItem().is(INSTANCE) && !player.getPersistentData().contains(STATE)) return;
        var data = state(player); int energy = data.getInt("energy"); boolean overloaded = data.getBoolean("overloaded");
        if (mode(player)) { if (energy < 600) energy = Math.min(600, energy + 2); else overloaded = true; }
        if (overloaded) { if (energy > 0) energy = Math.max(0, energy - 3); else overloaded = false; }
        data.putInt("energy", energy); data.putBoolean("overloaded", overloaded); projectState(player, data);
    }
    public static boolean attack(Player player) {
        var stack = player.getMainHandItem();
        if (player.level().isClientSide() || player.isSpectator() || !stack.is(INSTANCE) || player.getAttackStrengthScale(0) != 1 || overloaded(player)) return false;
        var previous = LAST_ATTACK.get(player); long now = player.level().getGameTime();
        if (previous != null && now - previous < Math.ceil(player.getCurrentItemAttackStrengthDelay())) return false;
        if (player.hasEffect(ExtraBotanyMobEffects.FLAMESCION)) {
            for (int i = 0; i < 3; i++) player.level().addFreshEntity(LegacyFlameProjectile.create(true, player,
                    player.getLookAngle().yRot((float) Math.toRadians(-15 + 15 * i)).normalize()));
            player.removeEffect(ExtraBotanyMobEffects.FLAMESCION);
        } else if (mode(player)) player.level().addFreshEntity(LegacyFlameProjectile.create(false, player, player.getLookAngle().normalize()));
        else return false;
        LAST_ATTACK.put(player, now); return true;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player.isSpectator() || overloaded(player) || player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.pass(stack);
        if (player.isShiftKeyDown() && !mode(player)) {
            boolean grounded = player.onGround();
            if (grounded) {
                // Predict the original lift on the client too. Clear contact before the effect ticks;
                // otherwise the server removes ignition while waiting for the first airborne movement packet.
                player.setDeltaMovement(player.getDeltaMovement().add(0, 1, 0));
                player.setOnGround(false); player.hurtMarked = true;
            }
            if (!level.isClientSide()) {
                if (grounded) for (var target : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(3))) {
                    if (target == player) continue;
                    target.setDeltaMovement(target.getDeltaMovement().add(0, 1, 0)); target.hurtMarked = true;
                    target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 60));
                }
                player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 60));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (mode(player)) {
            if (!level.isClientSide()) {
                level.addFreshEntity(LegacyFlameArea.create(LegacyFlameArea.Kind.VOID, player, player.position().add(player.getLookAngle().scale(5))));
                player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 80)); player.getCooldowns().addCooldown(this, 40);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }
    public static void ultimate(Player player) {
        if (player.level().isClientSide() || player.isSpectator() || !mode(player)) return;
        var start = player.position().add(player.getLookAngle().normalize().scale(5));
        player.level().addFreshEntity(LegacyFlameArea.create(LegacyFlameArea.Kind.ULT, player, new net.minecraft.world.phys.Vec3(start.x, player.getY() + .25, start.z)));
        var data = state(player); data.putInt("energy", 600); data.putBoolean("overloaded", true); projectState(player, data);
        player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 40));
    }
    public static void dash(Player player) {
        if (player.level().isClientSide() || player.isSpectator() || !mode(player) || player.getCooldowns().isOnCooldown(INSTANCE)) return;
        var start = player.position(); var movement = player.getLookAngle().normalize().scale(4); var end = start.add(movement);
        player.teleportTo(end.x, end.y, end.z);
        boolean hit = false;
        var source = io.github.lounode.extrabotany.common.ExtraBotanyDamageTypes.Sources.source(player.level().registryAccess(),
                net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, net.minecraft.resources.ResourceLocation.parse("extrabotany:flamescion_flame")));
        for (var target : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8))) {
            if (target == player || target.getBoundingBox().inflate(4).clip(start.subtract(movement), end.add(movement)).isEmpty()) continue;
            target.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.TIMELOCK, 40)); target.invulnerableTime = 0; target.hurt(source, 6); hit = true;
        }
        if (hit) { player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.INCANDESCENCE, 80)); player.addEffect(new MobEffectInstance(ExtraBotanyMobEffects.FLAMESCION, 200)); }
        player.getCooldowns().addCooldown(INSTANCE, 20);
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.controls").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.ultimate_key",
                Component.keybind("key.extrabotany.legacy_skill")).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.energy", energy(stack), 600).withStyle(ChatFormatting.GRAY));
        if (overloaded(stack)) tooltip.add(Component.translatable("tooltip.extrabotany.flamescion_weapon.overloaded").withStyle(ChatFormatting.RED));
    }
}
