package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyFlameArea;
import io.github.lounode.extrabotany.common.entity.LegacyFlameProjectile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;

/** Direct translation of the five 1.16 renderers, including their original text effects. */
public final class LegacyFlameRenderer<T extends Entity> extends EntityRenderer<T> {
    public static final ResourceLocation STRENGTHEN_MODEL = ResourceLocation.parse("extrabotany:icon/strengthenslash");
    private final ModelPart slash;
    public LegacyFlameRenderer(EntityRendererProvider.Context context) {
        super(context);
        var mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-16, -32, 0, 32, 32, 0), PartPose.offset(0, 24, 0));
        slash = LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }
    @Override public ResourceLocation getTextureLocation(T entity) {
        return entity instanceof LegacyFlameArea area && area.kind() == LegacyFlameArea.Kind.SLASH
                ? ResourceLocation.parse("extrabotany:textures/entity/flamescionslash_" + entity.tickCount % 6 + ".png") : InventoryMenu.BLOCK_ATLAS;
    }
    @Override public boolean shouldRender(T entity, Frustum frustum, double x, double y, double z) {
        return entity.shouldRender(x, y, z) && frustum.isVisible(entity.getBoundingBox().inflate(6));
    }
    @Override public void render(T entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        if (entity.getType() == LegacyFlameProjectile.STRENGTHEN) {
            renderStrengthen(entity, pose, buffers);
        } else {
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            if (entity instanceof LegacyFlameArea area && area.kind() == LegacyFlameArea.Kind.SLASH) {
                pose.scale(3, -3, -3);
                pose.mulPose(Axis.XP.rotationDegrees(area.pitch())); pose.mulPose(Axis.YP.rotationDegrees(area.rotation()));
                slash.render(pose, buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(getTextureLocation(entity))), 0xF000F0, OverlayTexture.NO_OVERLAY);
            } else if (entity instanceof LegacyFlameArea area && area.kind() == LegacyFlameArea.Kind.ULT) {
                float size = 4F / 60; pose.scale(-size, -size, size); pose.translate(0, -60, 0);
                for (int i = 0; i < 8; i++) { pose.translate(0, 12, 0); text("mri.ult" + i, ChatFormatting.DARK_RED, pose, buffers, light); }
            } else {
                boolean isVoid = entity instanceof LegacyFlameArea;
                if (isVoid) pose.mulPose(Axis.ZN.rotationDegrees((entity.tickCount + partial) * 10));
                float size = (isVoid ? 5F : 7F) / 60; pose.scale(-size, -size, size);
                text(isVoid ? "mri.void" : "mri.sword", isVoid ? ChatFormatting.GOLD : ChatFormatting.RED, pose, buffers, light);
            }
        }
        pose.popPose(); super.render(entity, yaw, partial, pose, buffers, light);
    }
    private void text(String key, ChatFormatting color, PoseStack pose, MultiBufferSource buffers, int light) {
        var text = Component.translatable(key).withStyle(color); var font = getFont();
        font.drawInBatch(text, -font.width(text) / 2F, 0, -1, false, pose.last().pose(), buffers, Font.DisplayMode.NORMAL, 0, light);
    }
    @SuppressWarnings("deprecation")
    private void renderStrengthen(T entity, PoseStack pose, MultiBufferSource buffers) {
        pose.scale(3.5F, 3.5F, 3.5F);
        pose.mulPose(Axis.YP.rotationDegrees(entity.getYRot() + 270));
        pose.mulPose(Axis.ZP.rotationDegrees(entity.getXRot() + 30));
        pose.translate(-.5, -.5, -.5);
        var model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(STRENGTHEN_MODEL));
        var vertices = buffers.getBuffer(Sheets.translucentItemSheet()); var random = RandomSource.create(42);
        for (int side = 0; side <= 6; side++) {
            random.setSeed(42);
            for (var quad : model.getQuads(null, side == 6 ? null : Direction.values()[side], random))
                vertices.putBulkData(pose.last(), quad, 1, 1, 1, .8F, 0xF000F0, OverlayTexture.NO_OVERLAY);
        }
    }
}
