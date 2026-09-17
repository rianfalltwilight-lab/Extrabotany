package io.github.lounode.extrabotany.forge.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import com.mojang.blaze3d.platform.InputConstants;

@EventBusSubscriber(modid = "extrabotany", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LegacyKeyMappings {
    public static final KeyMapping SKILL = new KeyMapping("key.extrabotany.legacy_skill", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, 82, "key.categories.extrabotany");
    @SubscribeEvent public static void register(RegisterKeyMappingsEvent event) { event.register(SKILL); }
}
