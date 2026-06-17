package com.norako.fracturia.entity.client.end.endersent;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.end.endersent.CommonEndersentEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class EndersentRenderer extends GeoEntityRenderer<CommonEndersentEntity> {

    public EndersentRenderer(EntityRendererFactory.Context context) {
        super(context, new EndersentModel());
    }

    @Override
    public Identifier getTextureLocation(CommonEndersentEntity entity) {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/endersent/endersent.png");
    }
}
