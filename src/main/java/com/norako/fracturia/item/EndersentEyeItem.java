package com.norako.fracturia.item;

import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class EndersentEyeItem extends Item {

    private final String descriptionKey;

    public EndersentEyeItem(String descriptionKey) {
        super(new Item.Settings());
        this.descriptionKey = descriptionKey;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(descriptionKey).formatted(Formatting.GRAY));
    }
}