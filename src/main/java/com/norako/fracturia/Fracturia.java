package com.norako.fracturia;

import com.norako.fracturia.block.FracturiaBlocks;
import com.norako.fracturia.entity.FracturiaEntities;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.MountaineerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.SquallGolemEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.WindcallerEntity;
import com.norako.fracturia.item.FracturiaItemGroups;
import com.norako.fracturia.item.FracturiaItems;
import com.norako.fracturia.sound.FracturiaSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fracturia implements ModInitializer {
	public static final String MOD_ID = "fracturia";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
    {
        FracturiaItems.registerFracturiaItems();
        FracturiaBlocks.registerFracturiaBlocks();
        FracturiaItemGroups.registerItemGroups();
        FracturiaEntities.registerFracturiaEntities();
        FracturiaSounds.registerSounds();

        FabricDefaultAttributeRegistry.register(FracturiaEntities.MOUNTAINEER_ENTITY, MountaineerEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.WINDCALLER_ENTITY, WindcallerEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.SQUALL_GOLEM_ENTITY, SquallGolemEntity.setAttributes());
        FabricDefaultAttributeRegistry.register(FracturiaEntities.ILLUSIONER_ENTITY, IllusionerEntity.setAttributes());
    }
}