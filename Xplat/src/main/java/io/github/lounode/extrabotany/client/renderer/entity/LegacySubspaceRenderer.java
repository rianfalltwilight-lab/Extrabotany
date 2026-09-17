package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacySubspace;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Six original frames and the original delay/growth/fade curve from master RenderSubspace. */
public final class LegacySubspaceRenderer extends EntityRenderer<LegacySubspace> {
    public LegacySubspaceRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(LegacySubspace entity) {
        return ResourceLocation.fromNamespaceAndPath("extrabotany", "textures/misc/subspace_" + Math.floorMod(entity.tickCount, 6) + ".png");
    }
    @Override public boolean shouldRender(LegacySubspace entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z) && frustum.isVisible(entity.getBoundingBox().inflate(entity.visualSize()));
    }
    @Override public void render(LegacySubspace entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        float scale = entity.renderScale(partial);
        if (scale <= 0) return;
        pose.pushPose(); pose.mulPose(Axis.YP.rotationDegrees(entity.rotation())); pose.scale(scale, scale, scale);
        var vertices = buffers.getBuffer(LegacyPortalRenderType.get(getTextureLocation(entity)));
        for (int i = 0; i < 4; i++) {
            boolean right = i >= 2, top = i == 0 || i == 3;
            vertices.addVertex(pose.last().pose(), right ? 1 : -1, top ? -1 : 1, 0)
                    .setColor(-1).setUv(right ? 1 : 0, top ? 0 : 1);
        }
        pose.popPose(); super.render(entity, yaw, partial, pose, buffers, light);
    }
}
