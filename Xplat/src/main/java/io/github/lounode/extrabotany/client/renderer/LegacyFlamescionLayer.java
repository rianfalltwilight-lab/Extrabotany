package io.github.lounode.extrabotany.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.item.legacy.LegacyFlamescionItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/** Original 1.16 LayerFlamescion transforms, alpha pulse and animated ring artwork. */
public final class LegacyFlamescionLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public static final ResourceLocation MODEL = ResourceLocation.parse("extrabotany:icon/flamescionring");
    public LegacyFlamescionLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) { super(renderer); }
    @Override @SuppressWarnings("deprecation")
    public void render(PoseStack pose, MultiBufferSource buffers, int packedLight, AbstractClientPlayer player,
            float limb, float amount, float partial, float age, float yaw, float pitch) {
        if (!player.getMainHandItem().is(LegacyFlamescionItem.INSTANCE)) return;
        pose.pushPose(); getParentModel().body.translateAndRotate(pose);
        pose.translate(-.6, -.6, 0); pose.scale(1.2F, 1.2F, 1.2F);
        pose.mulPose(Axis.YP.rotationDegrees(-20)); pose.mulPose(Axis.ZP.rotationDegrees(-40));
        pose.mulPose(Axis.XP.rotationDegrees(100)); pose.mulPose(Axis.ZN.rotationDegrees(age / 5));
        pose.translate(-.5, -.5, -.5); // Upstream RenderHelper centers the baked item model.
        var model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(MODEL));
        var vertices = buffers.getBuffer(Sheets.translucentItemSheet()); var random = RandomSource.create(42);
        float alpha = (float) (.75 + .15 * Math.cos(age / 20)); int light = (int) (0xF000B0 + 0x30 * Math.cos(age / 20));
        for (int side = 0; side <= 6; side++) {
            random.setSeed(42);
            for (var quad : model.getQuads(null, side == 6 ? null : Direction.values()[side], random))
                vertices.putBulkData(pose.last(), quad, 1, 1, 1, alpha, light, OverlayTexture.NO_OVERLAY);
        }
        pose.popPose();
    }
}
