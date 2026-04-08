package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class IllusionerModel extends GeoModel<IllusionerEntity>
{
    @Override
    public Identifier getModelResource(IllusionerEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "geo/illusioner.geo.json");
    }

    @Override
    public Identifier getTextureResource(IllusionerEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/illager/illusioner.png");
    }

    @Override
    public Identifier getAnimationResource(IllusionerEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "animations/illusioner.animation.json");
    }
}
