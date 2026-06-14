package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerCloneEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class IllusionerCloneModel extends GeoModel<IllusionerCloneEntity>
{
    @Override
    public Identifier getModelResource(IllusionerCloneEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "geo/illusioner.geo.json");
    }

    @Override
    public Identifier getTextureResource(IllusionerCloneEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/illager/illusioner.png");
    }

    @Override
    public Identifier getAnimationResource(IllusionerCloneEntity animatable)
    {
        return Identifier.of(Fracturia.MOD_ID, "animations/illusioner.animation.json");
    }
}
