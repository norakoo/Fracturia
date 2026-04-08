package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.WindcallerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WindcallerRenderer extends GeoEntityRenderer<WindcallerEntity>
{
    public WindcallerRenderer(EntityRendererFactory.Context renderManager)
    {
        super(renderManager, new WindcallerModel());
        this.shadowRadius = 0.5f;
        this.addRenderLayer(new BannerHeadLayerWindcaller(this));
    }

    @Override
    public Identifier getTextureLocation(WindcallerEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/illager/windcaller.png");
    }

    @Override
    public void render(WindcallerEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight)
    {
        poseStack.scale(1.0f, 1.0f, 1.0f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
