package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.SquallGolemEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class SquallGolemModel extends GeoModel<SquallGolemEntity> {
    @Override
    public Identifier getModelResource(SquallGolemEntity animatable) {
        return Identifier.of(Fracturia.MOD_ID, "geo/squall_golem.geo.json");
    }

    @Override
    public Identifier getTextureResource(SquallGolemEntity animatable) {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/illager/squall_golem.png");
    }

    @Override
    public Identifier getAnimationResource(SquallGolemEntity animatable) {
        return Identifier.of(Fracturia.MOD_ID, "animations/squall_golem.animation.json");
    }

    @Override
    public void setCustomAnimations(SquallGolemEntity animatable, long instanceId, AnimationState<SquallGolemEntity> animationState)
    {
        GeoBone head = getAnimationProcessor().getBone("bipedHead");

        if (head != null)
        {
            EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
            head.setRotX(entityData.headPitch() * MathHelper.RADIANS_PER_DEGREE);
            head.setRotY(entityData.netHeadYaw() * MathHelper.RADIANS_PER_DEGREE);
        }
    }
}
