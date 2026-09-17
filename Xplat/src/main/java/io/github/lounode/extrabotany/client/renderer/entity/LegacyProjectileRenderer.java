package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyProjectile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;

public final class LegacyProjectileRenderer extends EntityRenderer<LegacyProjectile> {
    public static final ResourceLocation BUTTERFLY=ResourceLocation.parse("extrabotany:icon/butterflyprojectile");
    public LegacyProjectileRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(LegacyProjectile entity) { return InventoryMenu.BLOCK_ATLAS; }
    @Override public void render(LegacyProjectile entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        pose.pushPose();
        if(entity.getType()==LegacyProjectile.TYPES.get(LegacyProjectile.Kind.BUTTERFLY)){
            pose.mulPose(entityRenderDispatcher.cameraOrientation());
            // 1.16 RenderHelper's centered generated item, adapted to the master icon helper coordinates.
            pose.translate(-.5,-.5,1F/32);LegacyIconRenderer.render(BUTTERFLY,pose,buffers,.9F);
        }else{
            pose.translate(.5,1.5,.5);pose.mulPose(Axis.YP.rotationDegrees((entity.tickCount+partial)*6));
            Minecraft.getInstance().getItemRenderer().renderStatic(entity.getItem(),ItemDisplayContext.GROUND,light,
                    OverlayTexture.NO_OVERLAY,pose,buffers,entity.level(),entity.getId());
        }
        pose.popPose();super.render(entity,yaw,partial,pose,buffers,light);
    }
}
