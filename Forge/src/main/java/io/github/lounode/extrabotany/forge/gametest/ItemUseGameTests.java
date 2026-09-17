package io.github.lounode.extrabotany.forge.gametest;

import io.github.lounode.extrabotany.common.item.legacy.*;
import io.github.lounode.extrabotany.common.entity.LegacySubspace;
import io.github.lounode.extrabotany.common.brew.ExtraBotanyMobEffects;
import io.github.lounode.extrabotany.xplat.EXplatAbstractions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.common.item.BotaniaItems;
import top.theillusivec4.curios.api.CuriosApi;

@GameTestHolder("extrabotany")
@PrefixGameTestTemplate(false)
public final class ItemUseGameTests {
    private static Player player(GameTestHelper h, ItemStack stack) {
        var p = h.makeMockPlayer(GameType.SURVIVAL);
        p.setPos(h.absolutePos(new BlockPos(3, 4, 3)).getCenter());
        p.setNoGravity(true);
        p.setItemInHand(InteractionHand.MAIN_HAND, stack);
        for (int i = 0; i < 30; i++) p.tick();
        return p;
    }
    private static ManaItem mana(Player p) {
        var tablet = new ItemStack(BotaniaItems.MANA_TABLET);
        p.getInventory().setItem(2, tablet);
        var mana = ManaItem.LOOKUP.find(tablet); mana.addMana(50000); return mana;
    }
    @GameTest(template = "empty", timeoutTicks = 30)
    public static void spearHoldingUntilCompletionFiresOnce(GameTestHelper h) {
        var item = LegacyRelicSword.ITEMS.get("spear_of_subspace");
        var stack = new ItemStack(item); var p = player(h, stack); var mana = mana(p);
        EXplatAbstractions.INSTANCE.findRelic(stack).bindToUUID(p.getUUID());
        item.use(h.getLevel(), p, InteractionHand.MAIN_HAND);
        for (int i = 0; i < item.getUseDuration(stack, p); i++) p.tick();
        h.assertTrue(mana.getMana() == 40000, "Holding spear to its full use duration silently did not fire");
        // The original 12-block fan crosses this tiny template's ticking chunk boundary.
        long portals = java.util.stream.StreamSupport.stream(h.getLevel().getAllEntities().spliterator(), false)
                .filter(e -> e instanceof LegacySubspace portal && portal.getOwner() == p).count();
        h.assertTrue(portals == 24, "Spear completion did not create exactly 24 portals: " + portals);
        item.releaseUsing(stack, h.getLevel(), p, 0);
        h.assertTrue(mana.getMana() == 40000, "Repeated completion bypassed cooldown and paid twice");
        h.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 30)
    public static void spearReleaseWorksAndEmptyManaCanRetry(GameTestHelper h) {
        var item = LegacyRelicSword.ITEMS.get("spear_of_subspace");
        var stack = new ItemStack(item); var p = player(h, stack);
        EXplatAbstractions.INSTANCE.findRelic(stack).bindToUUID(p.getUUID());
        item.use(h.getLevel(), p, InteractionHand.MAIN_HAND); p.releaseUsingItem();
        h.assertTrue(!p.getCooldowns().isOnCooldown(item), "Empty mana incorrectly locks spear for one minute");
        var mana = mana(p);
        item.use(h.getLevel(), p, InteractionHand.MAIN_HAND); p.releaseUsingItem();
        h.assertTrue(mana.getMana() == 40000 && p.hasEffect(ExtraBotanyMobEffects.ETERNITY), "Spear release after refilling mana failed");
        h.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 30)
    public static void conquestHasUsableSlotAndRealCapability(GameTestHelper h) {
        var item = LegacyAccessories.ITEMS.get("gem_of_conquest");
        var stack = new ItemStack(item); var p = player(h, ItemStack.EMPTY);
        h.assertTrue(stack.is(ItemTags.create(ResourceLocation.parse("curios:curio"))), "Conquest gem lacks a slot usable without third party charm slots");
        var curio = CuriosApi.getCurio(stack).orElseThrow();
        var inventory = CuriosApi.getCuriosInventory(p).orElseThrow();
        var entry = inventory.getCurios().entrySet().stream().filter(e -> e.getValue().getStacks().getSlots() > 0).findFirst().orElseThrow();
        var context = new top.theillusivec4.curios.api.SlotContext(entry.getKey(), p, 0, false, true);
        h.assertTrue(curio.canEquip(context), "Conquest gem Curios capability refused equip");
        var modifiers = curio.getAttributeModifiers(context, ResourceLocation.parse("extrabotany:test_slot"));
        h.assertTrue(modifiers.size() == 3, "Conquest modifiers missing through actual Curios capability");
        entry.getValue().getStacks().setStackInSlot(0, stack);
        h.runAfterDelay(2, () -> {
            h.assertTrue(LegacyAccessories.worn("gem_of_conquest", p), "Equipped conquest gem cannot be found after cache tick");
            h.succeed();
        });
    }
    @GameTest(template = "empty", timeoutTicks = 30)
    public static void flamescionEntersModeThroughUseAndCasts(GameTestHelper h) {
        var item = LegacyFlamescionItem.INSTANCE; var stack = new ItemStack(item); var p = player(h, stack);
        p.setOnGround(true); p.setShiftKeyDown(true);
        h.assertTrue(item.use(h.getLevel(), p, InteractionHand.MAIN_HAND).getResult().consumesAction(), "Sneak-use did not activate Flamescion");
        h.assertTrue(p.hasEffect(ExtraBotanyMobEffects.INCANDESCENCE), "Sneak-use did not grant ignition");
        p.setShiftKeyDown(false); p.setOnGround(false);
        h.assertTrue(LegacyFlamescionItem.mode(p) && LegacyFlamescionItem.attack(p), "Airborne ignition mode cannot fire");
        LegacyFlamescionItem.ultimate(p);
        h.assertTrue(LegacyFlamescionItem.overloaded(stack), "Ultimate failed through normal ignition state");
        h.succeed();
    }
}
