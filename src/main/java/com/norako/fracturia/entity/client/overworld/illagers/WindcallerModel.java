package com.norako.fracturia.entity.client.overworld.illagers;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.WindcallerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class WindcallerModel extends GeoModel<WindcallerEntity>
{

    @Override
    public Identifier getModelResource(WindcallerEntity animatable) {
        return Identifier.of(Fracturia.MOD_ID, "geo/windcaller.geo.json");
    }

    @Override
    public Identifier getTextureResource(WindcallerEntity animatable) {
        return Identifier.of(Fracturia.MOD_ID, "textures/entity/illager/windcaller.png");
    }

    @Override
    public Identifier getAnimationResource(WindcallerEntity animatable) {
        return Identifier.of(Fracturia.MOD_ID, "animations/windcaller.animation.json");
    }

    @Override
    public void setCustomAnimations(WindcallerEntity animatable, long instanceId, AnimationState<WindcallerEntity> animationState)
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
