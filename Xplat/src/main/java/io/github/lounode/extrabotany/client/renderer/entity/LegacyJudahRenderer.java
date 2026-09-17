package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyJudahEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/** Master RenderJudahOath / RenderJudahSpear, with their dedicated entity sprites. */
public final class LegacyJudahRenderer extends EntityRenderer<LegacyJudahEntity> {
    public LegacyJudahRenderer(EntityRendererProvider.Context context) { super(context); }
    public static ResourceLocation model(boolean spear, int variant) {
        return ResourceLocation.parse("extrabotany:icon/" + (spear ? "spear_" : "judahoath_") + variant);
    }
    @Override public ResourceLocation getTextureLocation(LegacyJudahEntity entity) { return InventoryMenu.BLOCK_ATLAS; }
    @Override public boolean shouldRender(LegacyJudahEntity entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z) && frustum.isVisible(entity.getBoundingBox().inflate(4));
    }
    @Override public void render(LegacyJudahEntity entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        if (entity.isSpear()) {
            pose.translate(-.05, 0, 1.15); pose.mulPose(Axis.YP.rotationDegrees(-90)); pose.mulPose(Axis.ZP.rotationDegrees(135));
            pose.scale(1.8F, 1.8F, 1.8F);
            LegacyIconRenderer.render(model(true, Math.min(entity.variant(), 1) * 2 + (entity.fake() ? 1 : 0)), pose, buffers, 1);
        } else {
            pose.translate(0, 3, 0); pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
            for (int side = 0; side < 2; side++) {
                pose.pushPose(); if (side == 1) pose.scale(-1, 1, 1);
                pose.mulPose(Axis.ZP.rotationDegrees(180)); pose.scale(2.64F, 2.64F, 2.64F);
                LegacyIconRenderer.render(model(false, entity.variant()), pose, buffers, 1); pose.popPose();
            }
        }
        pose.popPose(); super.render(entity, yaw, partial, pose, buffers, light);
    }
}
