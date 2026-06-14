package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
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
}
