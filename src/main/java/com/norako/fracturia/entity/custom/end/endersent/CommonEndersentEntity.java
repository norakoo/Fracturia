package com.norako.fracturia.entity.custom.end.endersent;

import com.norako.fracturia.world.EndersentWorldState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.world.World;

public class CommonEndersentEntity extends EndersentEntity {

    public CommonEndersentEntity(EntityType<? extends CommonEndersentEntity> type, World world) {
        super(type, world);
    }

    @Override
    public EndersentWorldState.Variant getVariant() {
        return null;
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return EndersentEntity.setAttributes()
                .add(net.minecraft.entity.attribute.EntityAttributes.GENERIC_MAX_HEALTH, 40.0);
    }

    @Override
    protected String getBossBarName() { return "Endersent"; }

    @Override
    protected BossBar.Color getBossBarColor() { return BossBar.Color.PINK; }
}