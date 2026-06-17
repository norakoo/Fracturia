package com.norako.fracturia.entity.custom.end.endersent;

import com.norako.fracturia.item.FracturiaItems;
import com.norako.fracturia.world.EndersentWorldState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class SavageEndersentEntity extends EndersentEntity {

    public SavageEndersentEntity(EntityType<? extends SavageEndersentEntity> type, World world) {
        super(type, world);
    }

    @Override
    public EndersentWorldState.Variant getVariant() {
        return EndersentWorldState.Variant.SAVAGE;
    }

    public static DefaultAttributeContainer.Builder setAttributes(){
        return EndersentEntity.setAttributes();
    }

    @Override
    protected String getBossBarName() { return "§cSavage Endersent"; }

    @Override
    protected BossBar.Color getBossBarColor() { return BossBar.Color.RED; }
}
