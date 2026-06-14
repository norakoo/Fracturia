package com.norako.fracturia.entity.client.overworld.whisperer;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.whisperer.PoisonVineEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class PoisonVineModel extends GeoModel<PoisonVineEntity>
{
    @Override
    public Identifier getModelResource(PoisonVineEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "geo/poison_vine.geo.json");
    }

    @Override
    public Identifier getTextureResource(PoisonVineEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/whisperer/poison_quill_vine.png");
    }

    @Override
    public Identifier getAnimationResource(PoisonVineEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "animations/poison_vine.animation.json");
    }
}
