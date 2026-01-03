package com.norako.fracturia;

import com.norako.fracturia.block.FracturiaBlocks;
import com.norako.fracturia.item.FracturiaItemGroups;
import com.norako.fracturia.item.FracturiaItems;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fracturia implements ModInitializer {
	public static final String MOD_ID = "fracturia";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
    {
        FracturiaItems.registerFracturiaItems();
        FracturiaBlocks.registerModBlocks();
        FracturiaItemGroups.registerItemGroups();
	}
}