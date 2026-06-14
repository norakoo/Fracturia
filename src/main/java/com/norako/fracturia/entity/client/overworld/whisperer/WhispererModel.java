package com.norako.fracturia.entity.client.overworld.whisperer;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.whisperer.WhispererEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class WhispererModel extends GeoModel<WhispererEntity>
{
    @Override
    public Identifier getModelResource(WhispererEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "geo/whisperer.geo.json");
    }

    @Override
    public Identifier getTextureResource(WhispererEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/whisperer/whisperer.png");
    }

    @Override
    public Identifier getAnimationResource(WhispererEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "animations/whisperer.animation.json");
    }
}
