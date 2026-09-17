package io.github.lounode.extrabotany.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lounode.extrabotany.common.block.legacy.LegacyMachineEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** Master quantum buffer tile model and its exact world-space transform. */
public final class LegacyQuantumRenderer implements BlockEntityRenderer<LegacyMachineEntity> {
    private final ModelPart model=LegacyQuantumModel.create();
    private static final ResourceLocation TEXTURE=ResourceLocation.parse("extrabotany:textures/model/quantummanabuffer.png");
    public LegacyQuantumRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public void render(LegacyMachineEntity entity,float partial,PoseStack pose,MultiBufferSource buffers,int light,int overlay) {
        pose.pushPose();pose.translate(.5,1.5,.5);pose.scale(1,-1,-1);
        model.render(pose,buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,overlay);pose.popPose();
    }
}
