package com.norako.fracturia.entity.client.end.endersent;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.end.endersent.EndersentEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class EndersentVariantRenderer extends GeoEntityRenderer<EndersentEntity> {

    public EndersentVariantRenderer(EntityRendererFactory.Context context) {
        super(context, new EndersentVariantModel());
        this.addRenderLayer(new GeoRenderLayer<EndersentEntity>(this) {
            @Override
            public void render(MatrixStack poseStack, EndersentEntity animatable, BakedGeoModel bakedModel,
                               RenderLayer renderType, VertexConsumerProvider bufferSource,
                               VertexConsumer buffer, float partialTick,
                               int packedLight, int packedOverlay) {
                Identifier glowTexture = Identifier.of(Fracturia.MOD_ID,
                        "textures/entity/endersent/" + animatable.getVariant().name().toLowerCase() + "_glowing.png");
                RenderLayer emissiveLayer = RenderLayer.getEyes(glowTexture);
                VertexConsumer emissiveBuffer = bufferSource.getBuffer(emissiveLayer);
                getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable,
                        emissiveLayer, emissiveBuffer, partialTick, 15728880, packedOverlay,
                        0xFFFFFFFF);
            }
        });
    }

    @Override
    public Identifier getTextureLocation(EndersentEntity entity) {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/endersent/" + entity.getVariant().name().toLowerCase() + ".png");
    }
}
