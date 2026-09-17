package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyMount;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Complete original 1.16 motor/UFO models and their render transforms. */
public final class LegacyMountRenderer extends EntityRenderer<LegacyMount> {
    private final boolean flying;
    private final ModelPart model;
    private final ResourceLocation texture;
    public LegacyMountRenderer(EntityRendererProvider.Context context, boolean flying) {
        super(context); this.flying = flying;
        model = (flying ? LegacyMountModels.ufo() : LegacyMountModels.motor()).bakeRoot();
        texture = ResourceLocation.parse("extrabotany:textures/entity/" + (flying ? "ufo" : "motor") + ".png");
    }
    @Override public void render(LegacyMount mount,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose(); pose.translate(0,flying?2.5:1.5,0); pose.mulPose(Axis.YP.rotationDegrees(180-yaw));
        if(!flying) { pose.mulPose(Axis.ZP.rotationDegrees(mount.lean())); pose.mulPose(Axis.XP.rotationDegrees(mount.pitch())); }
        pose.scale(flying?-1.35F:-1,flying?-1.35F:-1,flying?1.35F:1);
        var vertex=buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        if (flying) {
            model.getChild("body").render(pose,vertex,light,OverlayTexture.NO_OVERLAY,-1);
            model.getChild("glow").render(pose,vertex,(int)(0xF00090 + 0x60 * Math.sin((mount.tickCount + partial) * .15)),OverlayTexture.NO_OVERLAY,-1);
        } else {
            var all = model.getChild("all");
            if (mount.getDeltaMovement().lengthSqr() > 0) for (String name : new String[]{"tirefront3", "tireback"})
                all.getChild(name).xRot = Mth.wrapDegrees(40 * mount.ridingTicks());
            all.render(pose,vertex,light,OverlayTexture.NO_OVERLAY,-1);
        }
        pose.popPose(); super.render(mount,yaw,partial,pose,buffers,light);
    }
    @Override public ResourceLocation getTextureLocation(LegacyMount mount) { return texture; }
}
