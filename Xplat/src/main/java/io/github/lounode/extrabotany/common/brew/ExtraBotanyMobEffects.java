package io.github.lounode.extrabotany.common.brew;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import vazkii.botania.common.helper.RegistryHelper;

import io.github.lounode.extrabotany.common.brew.effect.*;
import io.github.lounode.extrabotany.common.lib.LibPotionNames;

import java.util.ArrayList;
import java.util.List;

import static io.github.lounode.extrabotany.common.lib.ResourceLocationHelper.prefix;

public class ExtraBotanyMobEffects {
	private static final List<RegistryHelper.HolderProxy<MobEffect>> TO_REGISTER = new ArrayList<>();
	public static Holder<MobEffect> ETERNITY = create("eternity", new MobEffect(MobEffectCategory.BENEFICIAL, 14329120) {});
	public static Holder<MobEffect> INCANDESCENCE = create("incandescence", new IncandescenceMobEffect());
	public static Holder<MobEffect> FLAMESCION = create("flamescion", new MobEffect(MobEffectCategory.BENEFICIAL, 16729344) {});
	public static Holder<MobEffect> WITCH_CURSE = create("witch_curse", new MobEffect(MobEffectCategory.HARMFUL, 4915330) {});
	public static Holder<MobEffect> TIMELOCK = create("timelock", new TimeLockMobEffect());

	public static Holder<MobEffect> IMMOBILIZE = create(LibPotionNames.IMMOBILIZE,
			new ImmobilizeMobEffect(MobEffectCategory.HARMFUL, 9154528)
					.addAttributeModifier(Attributes.MOVEMENT_SPEED, prefix("immobilize"), -1.5D,
							AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	public static Holder<MobEffect> LINK = create(LibPotionNames.LINK, new LinkMobEffect(MobEffectCategory.HARMFUL, 9154528));
	public static Holder<MobEffect> HEAL_REVERSE = create(LibPotionNames.HEAL_REVERSE, new HealReverseMobEffect(MobEffectCategory.HARMFUL, 0X4B0082));
	public static Holder<MobEffect> DISCOUNT = create(LibPotionNames.DISCOUNT, new DiscountMobEffect(MobEffectCategory.NEUTRAL, 0x54eb89));
	public static Holder<MobEffect> WARM = create(LibPotionNames.WARM, new WarmMobEffect(MobEffectCategory.BENEFICIAL, 16750848));
	public static Holder<MobEffect> THIRROR = create(LibPotionNames.THIRROR, new ThirrorMobEffect(MobEffectCategory.BENEFICIAL, 0X4169E1));

	private static Holder<MobEffect> create(String name, MobEffect effect) {
		var proxy = RegistryHelper.holderProxy(Registries.MOB_EFFECT, prefix(name), effect);
		TO_REGISTER.add(proxy);
		return proxy;
	}

	public static void registerPotions(Registry<MobEffect> registry) {
        TO_REGISTER.forEach(proxy -> proxy.register(registry));
        // Active-effect maps use Holder identity. A proxy differs from the holder produced by
        // network/save codecs, so all public references must resolve to the registered holders.
        ETERNITY = registry.getHolderOrThrow(ETERNITY.unwrapKey().orElseThrow());
        INCANDESCENCE = registry.getHolderOrThrow(INCANDESCENCE.unwrapKey().orElseThrow());
        FLAMESCION = registry.getHolderOrThrow(FLAMESCION.unwrapKey().orElseThrow());
        WITCH_CURSE = registry.getHolderOrThrow(WITCH_CURSE.unwrapKey().orElseThrow());
        TIMELOCK = registry.getHolderOrThrow(TIMELOCK.unwrapKey().orElseThrow());
        IMMOBILIZE = registry.getHolderOrThrow(IMMOBILIZE.unwrapKey().orElseThrow());
        LINK = registry.getHolderOrThrow(LINK.unwrapKey().orElseThrow());
        HEAL_REVERSE = registry.getHolderOrThrow(HEAL_REVERSE.unwrapKey().orElseThrow());
        DISCOUNT = registry.getHolderOrThrow(DISCOUNT.unwrapKey().orElseThrow());
        WARM = registry.getHolderOrThrow(WARM.unwrapKey().orElseThrow());
        THIRROR = registry.getHolderOrThrow(THIRROR.unwrapKey().orElseThrow());

	}
}
