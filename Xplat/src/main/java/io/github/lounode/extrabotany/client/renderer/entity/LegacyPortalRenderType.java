package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import vazkii.botania.client.core.helper.CoreShaders;
import java.util.function.Function;

/** Both original portals use Botania's halo shader, with the user's shader setting respected. */
final class LegacyPortalRenderType extends RenderType {
    private LegacyPortalRenderType() {
        super("extrabotany_portal", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS,
                256, false, true, () -> {}, () -> {});
    }
    private static final Function<ResourceLocation, RenderType> TYPES = Util.memoize(texture -> create(
            "extrabotany_portal", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
            CompositeState.builder().setShaderState(new ShaderStateShard(CoreShaders::halo))
                    .setTextureState(new TextureStateShard(texture, false, false))
                    .setCullState(NO_CULL).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(false)));
    static RenderType get(ResourceLocation texture) { return TYPES.apply(texture); }
}
