package com.norako.fracturia.entity.client.overworld.whisperer;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.whisperer.WhispererEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WhispererRenderer extends GeoEntityRenderer<WhispererEntity>
{
    public WhispererRenderer(EntityRendererFactory.Context renderManager)
    {
        super(renderManager, new WhispererModel());
        this.shadowRadius = 0.5f;
    }

    @Override
    public Identifier getTextureLocation(WhispererEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/whisperer/whisperer.png");
    }
}
