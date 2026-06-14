package com.norako.fracturia.entity.client.overworld.whisperer;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.whisperer.PoisonVineEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PoisonVineRenderer extends GeoEntityRenderer<PoisonVineEntity>
{
    public PoisonVineRenderer(EntityRendererFactory.Context renderManager)
    {
        super(renderManager, new PoisonVineModel());
        this.shadowRadius = 0.0f;
    }

    @Override
    public Identifier getTextureLocation(PoisonVineEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/whisperer/poison_quill_vine.png");
    }
}
