package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyLance;
import io.github.lounode.extrabotany.common.entity.LegacySwordDomain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;

/** Original master lance mesh, extruded sword icons and ModelVoid cube. */
public final class LegacyBossSupportRenderer<T extends Entity> extends EntityRenderer<T> {
    private final ModelPart spear = LegacySubspaceSpearRenderer.mesh();
    private final ModelPart field;
    public LegacyBossSupportRenderer(EntityRendererProvider.Context context) {
        super(context);
        var mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("Shape1", CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 16, 16, 16),
                PartPose.offsetAndRotation(-12, 3, -6, .7853982F, .7853982F, .7853982F));
        field = LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }
    public static ResourceLocation swordModel(int variety) { return ResourceLocation.parse("extrabotany:icon/sworddomain_" + Math.floorMod(variety, 10)); }
    @Override public ResourceLocation getTextureLocation(T entity) {
        return entity instanceof LegacySwordDomain ? InventoryMenu.BLOCK_ATLAS
                : ResourceLocation.parse(entity instanceof LegacyLance ? "extrabotany:textures/entity/spearsubspace.png" : "extrabotany:textures/model/void.png");
    }
    @Override public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z) && frustum.isVisible(entity.getBoundingBox().inflate(4));
    }
    @Override public void render(T entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        if (entity instanceof LegacyLance) {
            pose.translate(.5, 1.5, .5); pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot()));
            // Planted lances have pitch=-90, cancelling the original renderer's initial +90 rotation.
            pose.scale(1.12F, -1.12F, -1.12F);
            spear.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))), 0xF000F0, OverlayTexture.NO_OVERLAY);
        } else if (entity instanceof LegacySwordDomain domain) {
            pose.scale(3, 3, 3); pose.mulPose(Axis.YP.rotationDegrees(-90)); pose.mulPose(Axis.ZP.rotationDegrees(135));
            renderSword(domain, pose, buffers);
        } else {
            pose.translate(.5, 2, -.5); pose.scale(2.8F, -2.8F, -2.8F);
            field.render(pose, buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity))), light, OverlayTexture.NO_OVERLAY, 0x4CFF8C05);
        }
        pose.popPose(); super.render(entity, yaw, partial, pose, buffers, light);
    }
    @SuppressWarnings("deprecation")
    private void renderSword(LegacySwordDomain entity, PoseStack pose, MultiBufferSource buffers) {
        LegacyIconRenderer.render(swordModel(entity.variety()), pose, buffers, 1);
    }
}
