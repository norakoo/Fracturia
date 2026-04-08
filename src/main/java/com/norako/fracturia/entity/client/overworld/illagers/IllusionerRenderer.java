package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class IllusionerRenderer extends GeoEntityRenderer<IllusionerEntity>
{
    public IllusionerRenderer(EntityRendererFactory.Context renderManager)
    {
        super(renderManager, new IllusionerModel());
        this.shadowRadius = 0.5f;
        this.addRenderLayer(new BannerHeadLayerIllusioner(this));
    }

    @Override
    public Identifier getTextureLocation(IllusionerEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/illager/illusioner.png");
    }

    @Override
    public void render(IllusionerEntity entity, float entityYaw, float partialTick,
                       MatrixStack poseStack, VertexConsumerProvider bufferSource, int packedLight)
    {
        if (entity.isInvisible())
        {
            Vec3d[] offsets = entity.getMirrorCopyOffsets(partialTick);
            for (Vec3d offset : offsets)
            {
                poseStack.push();
                poseStack.translate(offset.x, offset.y, offset.z);
                entity.renderingAsPhantom = true;
                super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
                entity.renderingAsPhantom = false;
                poseStack.pop();
            }
        }
        else
        {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
}
