package com.norako.fracturia.entity.custom.end.endersent;

import com.norako.fracturia.item.FracturiaItems;
import com.norako.fracturia.world.EndersentWorldState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class RavenousEndersentEntity extends EndersentEntity {

    public RavenousEndersentEntity(EntityType<? extends RavenousEndersentEntity> type, World world) {
        super(type, world);
    }

    @Override
    public EndersentWorldState.Variant getVariant() {
        return EndersentWorldState.Variant.RAVENOUS;
    }

    public static DefaultAttributeContainer.Builder setAttributes(){
        return EndersentEntity.setAttributes();
    }

    @Override
    protected String getBossBarName() { return "§7Ravenous Endersent"; }

    @Override
    protected BossBar.Color getBossBarColor() { return BossBar.Color.WHITE; }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        if (!super.canSpawn(world, reason)) return false;
        return !world.isSkyVisible(this.getBlockPos()) && this.getBlockY() < 50;
    }
}
