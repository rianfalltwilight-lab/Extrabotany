package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/** Generated sprite extrusion in the original IconHelper coordinate system (z=-1/16..0). */
final class LegacyIconRenderer {
    private LegacyIconRenderer() {}
    @SuppressWarnings("deprecation")
    static void render(ResourceLocation id, PoseStack pose, MultiBufferSource buffers, float alpha) {
        var model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(id));
        var vertices = buffers.getBuffer(Sheets.translucentItemSheet()); var random = RandomSource.create(42);
        pose.pushPose(); pose.translate(0, 0, -8.5F / 16);
        for (int side = 0; side <= 6; side++) {
            random.setSeed(42);
            for (var quad : model.getQuads(null, side == 6 ? null : Direction.values()[side], random))
                vertices.putBulkData(pose.last(), quad, 1, 1, 1, alpha, 0xF000F0, OverlayTexture.NO_OVERLAY);
        }
        pose.popPose();
    }
}
