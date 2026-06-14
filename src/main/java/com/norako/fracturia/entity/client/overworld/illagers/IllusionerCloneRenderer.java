package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerCloneEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class IllusionerCloneRenderer extends GeoEntityRenderer<IllusionerCloneEntity>
{
    public IllusionerCloneRenderer(EntityRendererFactory.Context renderManager)
    {
        super(renderManager, new IllusionerCloneModel());
        this.shadowRadius = 0.5f;
        this.addRenderLayer(new BannerHeadLayerIllusionerClone(this));
    }

    @Override
    public Identifier getTextureLocation(IllusionerCloneEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/illager/illusioner.png");
    }
}
