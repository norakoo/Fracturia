package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.SquallGolemEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SquallGolemRenderer extends GeoEntityRenderer<SquallGolemEntity>
{
    public SquallGolemRenderer(EntityRendererFactory.Context renderManager)
    {
        super(renderManager, new SquallGolemModel());
        this.shadowRadius = 1f;
    }

    @Override
    public Identifier getTextureLocation(SquallGolemEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/illager/squall_golem.png");
    }

    @Override
    public void render(SquallGolemEntity entity, float entityYaw, float partialTick, MatrixStack poseStack,
                       VertexConsumerProvider bufferSource, int packedLight)
    {
        poseStack.scale(1.0f, 1.0f, 1.0f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
