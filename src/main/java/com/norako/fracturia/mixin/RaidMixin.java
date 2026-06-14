package com.norako.fracturia.mixin;

import com.norako.fracturia.entity.FracturiaEntities;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.MountaineerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.WindcallerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.raid.Raid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(Raid.class)
public class RaidMixin
{
    @Shadow @Final private ServerWorld world;

    @Inject(method = "spawnNextWave", at = @At("TAIL"))
    private void injectFracturiaRaiders(BlockPos pos, CallbackInfo ci)
    {
        Raid raid = (Raid) (Object) this;
        int wave = raid.getGroupsSpawned();

        Map<Integer, Set<RaiderEntity>> waveToRaiders = ((RaidAccessor) raid).getWaveToRaiders();
        Set<RaiderEntity> currentWaveRaiders = waveToRaiders.get(wave);

        boolean isSnowyBiome = this.world.getBiome(pos).value().getTemperature() < 0.15f;

        if (currentWaveRaiders != null && isSnowyBiome)
        {
            List<RaiderEntity> toRemove = new ArrayList<>();
            List<RaiderEntity> toAdd = new ArrayList<>();

            for (RaiderEntity raider : currentWaveRaiders)
            {
                if (raider.getType() == EntityType.VINDICATOR)
                {
                    MountaineerEntity mountaineer = FracturiaEntities.MOUNTAINEER_ENTITY.create(this.world);
                    if (mountaineer != null)
                    {
                        mountaineer.refreshPositionAndAngles(raider.getBlockPos(), raider.getYaw(), raider.getPitch());
                        toAdd.add(mountaineer);
                        toRemove.add(raider);
                    }
                }
            }

            for (RaiderEntity raider : toRemove)
            {
                raider.discard();
            }
            for (RaiderEntity mountaineer : toAdd)
            {
                raid.addRaider(wave, mountaineer, mountaineer.getBlockPos(), false);
            }
        }

        // Spawn Illusioner from wave 3 onwards (1 per wave, 2 from wave 5)
        if (wave >= 3)
        {
            int illusionerCount = wave >= 5 ? 2 : 1;
            for (int i = 0; i < illusionerCount; i++)
            {
                IllusionerEntity illusioner = FracturiaEntities.ILLUSIONER_ENTITY.create(this.world);
                if (illusioner != null)
                {
                    illusioner.refreshPositionAndAngles(pos, 0.0F, 0.0F);
                    raid.addRaider(wave, illusioner, pos, false);
                }
            }
        }
        if (wave >= 3)
        {
            int windcallerCount = wave >= 5 ? 2 : 1;
            for (int i = 0; i < windcallerCount; i++)
            {
                WindcallerEntity windcaller = FracturiaEntities.WINDCALLER_ENTITY.create(this.world);
                if (windcaller != null)
                {
                    windcaller.refreshPositionAndAngles(pos, 0.0F, 0.0F);
                    raid.addRaider(wave, windcaller, pos, false);
                }
            }
        }
    }
}
