package com.norako.fracturia.entity.custom.end.endersent;

import com.norako.fracturia.item.FracturiaItems;
import com.norako.fracturia.world.EndersentWorldState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class ReapingEndersentEntity extends EndersentEntity {

    public ReapingEndersentEntity(EntityType<? extends ReapingEndersentEntity> type, World world) {
        super(type, world);
    }

    @Override
    public EndersentWorldState.Variant getVariant() {
        return EndersentWorldState.Variant.REAPING;
    }

    public static DefaultAttributeContainer.Builder setAttributes(){
        return EndersentEntity.setAttributes();
    }

    @Override
    protected String getBossBarName() { return "§5Reaping Endersent"; }

    @Override
    protected BossBar.Color getBossBarColor() { return BossBar.Color.PURPLE; }

    @Override
    public boolean canSpawn(WorldAccess world, SpawnReason reason) {
        if (!super.canSpawn(world, reason)) return false;
        if (world instanceof ServerWorldAccess serverWorld) {
            ServerWorld sw = serverWorld.toServerWorld();
            var registry = sw.getRegistryManager().get(RegistryKeys.STRUCTURE);
            var key = RegistryKey.of(RegistryKeys.STRUCTURE, Identifier.ofVanilla("stronghold"));
            var entry = registry.getEntry(key);
            if (entry.isEmpty()) return false;
            return sw.getStructureAccessor()
                    .getStructureContaining(this.getBlockPos(), RegistryEntryList.of(entry.get()))
                    .hasChildren();
        }
        return false;
    }
}
