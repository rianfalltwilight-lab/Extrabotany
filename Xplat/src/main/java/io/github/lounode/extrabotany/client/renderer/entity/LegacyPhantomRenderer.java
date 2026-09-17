package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyPhantomSword;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class LegacyPhantomRenderer extends EntityRenderer<LegacyPhantomSword> {
    public LegacyPhantomRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public void render(LegacyPhantomSword entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        if (entity.delay() > 0) return;
        float alpha = entity.fake() ? Math.max(0, .6F - (entity.tickCount + partial) * .015F) : 1;
        if (alpha <= 0) return;
        pose.pushPose(); pose.scale(1.5F, 1.5F, 1.5F);
        // Modern yaw is atan2(dx,dz); upstream getRotation() is that angle +180.
        pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot() + 270));
        pose.mulPose(Axis.ZP.rotationDegrees(entity.getXRot() - 135));
        LegacyIconRenderer.render(LegacyBossSupportRenderer.swordModel(entity.variety()), pose, buffers, alpha);
        pose.popPose(); super.render(entity, yaw, partial, pose, buffers, light);
    }
    @Override public ResourceLocation getTextureLocation(LegacyPhantomSword entity) {
        return net.minecraft.world.inventory.InventoryMenu.BLOCK_ATLAS;
    }
}
