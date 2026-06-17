package com.norako.fracturia.block;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.sound.FracturiaSounds;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class FracturiaBlocks
{
    // OVERWORLD

    public static final Block DEEPSLATE_MAGMITITE_ORE = registerBlock("deepslate_magmitite_ore",
            new Block(AbstractBlock.Settings.create().strength(4f)
                    .requiresTool().sounds(BlockSoundGroup.DEEPSLATE)));

    public static final Block DEEPSLATE_XYOPHITE_ORE = registerBlock("deepslate_xyophite_ore",
            new Block(AbstractBlock.Settings.create().strength(4f)
                    .requiresTool().sounds(BlockSoundGroup.DEEPSLATE)));

    public static final Block DEEPSLATE_BELITE_ORE = registerBlock("deepslate_belite_ore",
            new Block(AbstractBlock.Settings.create().strength(4f)
                    .requiresTool().sounds(BlockSoundGroup.DEEPSLATE)));

    public static final Block DEEPSLATE_AVIDITE_ORE = registerBlock("deepslate_avidite_ore",
            new Block(AbstractBlock.Settings.create().strength(4f)
                    .requiresTool().sounds(BlockSoundGroup.DEEPSLATE)));

    // NETHER

    public static final Block NETHER_ONYX_ORE = registerBlock("nether_onyx_ore",
            new Block(AbstractBlock.Settings.copy(Blocks.NETHER_GOLD_ORE)));

    // END

    public static final Block ENDERLITE = registerBlock("enderlite",
            new Block(AbstractBlock.Settings.copy(Blocks.PURPUR_BLOCK)));

    public static final Block ENDERLITE_BRICKS = registerBlock("enderlite_bricks",
            new Block(AbstractBlock.Settings.copy(Blocks.PURPUR_BLOCK)));

    public static final Block CRACKED_ENDERLITE_BRICKS = registerBlock("cracked_enderlite_bricks",
            new Block(AbstractBlock.Settings.copy(Blocks.PURPUR_BLOCK)));

    public static final Block CHISELED_ENDERLITE_BRICKS = registerBlock("chiseled_enderlite_bricks",
            new Block(AbstractBlock.Settings.copy(Blocks.PURPUR_BLOCK)));

    public static final Block VOID_BLOCK = registerBlock("void_block",
            new VoidBlock(AbstractBlock.Settings.create()
                    .noCollision()
                    .replaceable()
                    .strength(0.2f)
                    .luminance(state -> 3)
                    .sounds(new BlockSoundGroup(1.0f, 1.0f,
                            FracturiaSounds.VOID_EFFECT_STOP,
                            FracturiaSounds.VOID_BLOCK_ENTER,
                            FracturiaSounds.VOID_BLOCK_ENTER,
                            FracturiaSounds.VOID_EFFECT_STOP,
                            FracturiaSounds.VOID_BLOCK_ENTER))));

    // ARKONIA

    // BIRUNIA

    // ICEOLIA

    // IRIDIA

    // LUMINIA

    // STELLARIA

    // VELIA

    // ZYPHIA

    // XYLOHIA

    // FRACTURIA

    private static Block registerBlock(String name, Block block)
    {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(Fracturia.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block)
    {
        Registry.register(Registries.ITEM, Identifier.of(Fracturia.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerFracturiaBlocks()
    {
        Fracturia.LOGGER.info("Registering Fracturia Blocks for " + Fracturia.MOD_ID);
    }
}
