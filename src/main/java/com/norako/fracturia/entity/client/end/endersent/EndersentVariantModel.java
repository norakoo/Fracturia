package com.norako.fracturia.entity.client.end.endersent;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.end.endersent.EndersentEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class EndersentVariantModel extends GeoModel<EndersentEntity> {

    @Override
    public Identifier getModelResource(EndersentEntity entity) {
        return Identifier.of(Fracturia.MOD_ID, "geo/endersent/endersent_variant.geo.json");
    }

    @Override
    public Identifier getTextureResource(EndersentEntity entity) {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/endersent/" + entity.getVariant().name().toLowerCase() + ".png");
    }

    @Override
    public Identifier getAnimationResource(EndersentEntity entity) {
        return Identifier.of(Fracturia.MOD_ID, "animations/endersent/endersent_variant.animation.json");
    }
}
