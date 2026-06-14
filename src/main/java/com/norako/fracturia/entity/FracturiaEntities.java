package com.norako.fracturia.entity;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerCloneEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.IllusionerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.MountaineerEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.SquallGolemEntity;
import com.norako.fracturia.entity.custom.overworld.whisperer.PoisonVineEntity;
import com.norako.fracturia.entity.custom.overworld.whisperer.WhispererEntity;
import com.norako.fracturia.entity.custom.overworld.illagers.WindcallerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class FracturiaEntities
{
    public static final EntityType<MountaineerEntity> MOUNTAINEER_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Fracturia.MOD_ID, "mountaineer"),
            EntityType.Builder.create(MountaineerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 2.1f).build());
    public static final EntityType<WindcallerEntity> WINDCALLER_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Fracturia.MOD_ID, "windcaller"),
            EntityType.Builder.create(WindcallerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.8f, 2.8f).build());
    public static final EntityType<SquallGolemEntity> SQUALL_GOLEM_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Fracturia.MOD_ID, "squall_golem"),
            EntityType.Builder.create(SquallGolemEntity::new, SpawnGroup.MONSTER)
                    .dimensions(2.0f, 2.5f).build());
    public static final EntityType<IllusionerEntity> ILLUSIONER_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Fracturia.MOD_ID, "illusioner"),
            EntityType.Builder.create(IllusionerEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 2.1f).build());
    public static final EntityType<IllusionerCloneEntity> ILLUSIONER_CLONE_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Fracturia.MOD_ID, "illusioner_clone"),
            EntityType.Builder.create(IllusionerCloneEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 2.1f).build());

    public static final EntityType<WhispererEntity> WHISPERER_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Fracturia.MOD_ID, "whisperer"),
            EntityType.Builder.create(WhispererEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.6f, 2.1f).build());
    public static final EntityType<PoisonVineEntity> POISON_VINE_ENTITY = Registry.register(Registries.ENTITY_TYPE,
            Identifier.of(Fracturia.MOD_ID, "poison_vine"),
            EntityType.Builder.<PoisonVineEntity>create(PoisonVineEntity::new, SpawnGroup.MONSTER)
                    .dimensions(0.7f, 4.0f).build());

    public static void registerFracturiaEntities()
    {
        Fracturia.LOGGER.info("Registering Entities for + ", Fracturia.MOD_ID);
    }
}
