package com.norako.fracturia.entity.client.end.endersent;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.end.endersent.CommonEndersentEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class EndersentModel extends GeoModel<CommonEndersentEntity> {

    @Override
    public Identifier getModelResource(CommonEndersentEntity entity) {
        return Identifier.of(Fracturia.MOD_ID, "geo/endersent/endersent.geo.json");
    }

    @Override
    public Identifier getTextureResource(CommonEndersentEntity entity) {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/endersent/endersent.png");
    }

    @Override
    public Identifier getAnimationResource(CommonEndersentEntity entity) {
        return Identifier.of(Fracturia.MOD_ID, "animations/endersent/endersent.animation.json");
    }
}
