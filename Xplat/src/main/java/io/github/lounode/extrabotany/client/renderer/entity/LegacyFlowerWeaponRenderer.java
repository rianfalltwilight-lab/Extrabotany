package io.github.lounode.extrabotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.lounode.extrabotany.common.entity.LegacyFlowerWeapon;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/** Original seventeen Garden of the King weapons and their charging portal. */
public final class LegacyFlowerWeaponRenderer extends EntityRenderer<LegacyFlowerWeapon> {
    private static final ResourceLocation PORTAL=ResourceLocation.parse("extrabotany:textures/misc/flowerweapon.png");
    public LegacyFlowerWeaponRenderer(EntityRendererProvider.Context context) { super(context); }
    public static ResourceLocation model(int index) { return ResourceLocation.parse("extrabotany:icon/flower_weapon_"+index); }
    @Override public ResourceLocation getTextureLocation(LegacyFlowerWeapon entity) { return InventoryMenu.BLOCK_ATLAS; }
    @Override public void render(LegacyFlowerWeapon entity,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light) {
        float charge=Math.min(10,Math.max(entity.liveTicks(),entity.chargeTicks())+partial),alpha=charge/10;
        if(alpha<=0)return;
        pose.pushPose();pose.mulPose(Axis.YP.rotationDegrees(entity.rotation()));
        pose.pushPose();pose.scale(1.5F,1.5F,1.5F);pose.mulPose(Axis.YP.rotationDegrees(-90));pose.mulPose(Axis.ZP.rotationDegrees(90));
        LegacyIconRenderer.render(model(entity.variety()),pose,buffers,alpha);pose.popPose();
        var random=new java.util.Random(entity.getUUID().getMostSignificantBits());
        pose.mulPose(Axis.XP.rotationDegrees(-90));pose.translate(0,-.3+random.nextFloat()*.1,1);
        float scale=alpha-(entity.liveTicks()>entity.delay()?Math.min(1,(entity.liveTicks()-entity.delay()+partial)*.2F):0);
        if(scale>0){
            pose.scale(scale*2,scale*2,scale*2);pose.mulPose(Axis.YP.rotationDegrees(charge*9+(entity.tickCount+partial)*.5F+random.nextFloat()*360));
            var vertices=buffers.getBuffer(LegacyPortalRenderType.get(PORTAL));
            for(int i=0;i<4;i++){
                boolean right=i>=2,top=i==0||i==3;
                vertices.addVertex(pose.last().pose(),right?1:-1,0,top?-1:1).setUv(right?1:0,top?0:1).setColor(1,1,1,alpha);
            }
        }
        pose.popPose();super.render(entity,yaw,partial,pose,buffers,light);
    }
}
