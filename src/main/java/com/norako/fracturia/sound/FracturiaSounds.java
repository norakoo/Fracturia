package com.norako.fracturia.sound;

import com.norako.fracturia.Fracturia;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class FracturiaSounds
{
    // OVERWORLD

    public static final SoundEvent MOUNTAINEER_AMBIENT = registerSoundEvent("mountaineer_ambient");
    public static final SoundEvent MOUNTAINEER_HURT = registerSoundEvent("mountaineer_hurt");
    public static final SoundEvent MOUNTAINEER_DEATH = registerSoundEvent("mountaineer_death");

    public static final SoundEvent WINDCALLER_AMBIENT = registerSoundEvent("windcaller_ambient");
    public static final SoundEvent WINDCALLER_HURT = registerSoundEvent("windcaller_hurt");
    public static final SoundEvent WINDCALLER_DEATH = registerSoundEvent("windcaller_death");
    public static final SoundEvent WINDCALLER_BLAST = registerSoundEvent("windcaller_blast");
    public static final SoundEvent WINDCALLER_BLAST_ATTACK = registerSoundEvent("windcaller_blast_attack");
    public static final SoundEvent WINDCALLER_LIFT = registerSoundEvent("windcaller_lift");
    public static final SoundEvent WINDCALLER_TORNADO = registerSoundEvent("windcaller_tornado");
    public static final SoundEvent WINDCALLER_POLNAREFF = registerSoundEvent("wincaller_polnareff");

    public static final SoundEvent SQUALL_GOLEM_AMBIENT = registerSoundEvent("squall_golem_ambient");
    public static final SoundEvent SQUALL_GOLEM_HURT = registerSoundEvent("squall_golem_hurt");
    public static final SoundEvent SQUALL_GOLEM_DEATH = registerSoundEvent("squall_golem_death");
    public static final SoundEvent SQUALL_GOLEM_POWER_ON = registerSoundEvent("squall_golem_power_on");
    public static final SoundEvent SQUALL_GOLEM_ATTACK = registerSoundEvent("squall_golem_attack");
    public static final SoundEvent SQUALL_GOLEM_LEFT_STEP = registerSoundEvent("squall_golem_left_step");
    public static final SoundEvent SQUALL_GOLEM_RIGHT_STEP = registerSoundEvent("squall_golem_right_step");

    public static final SoundEvent ILLUSIONER_AMBIENT = registerSoundEvent("illusioner_ambient");
    public static final SoundEvent ILLUSIONER_HURT = registerSoundEvent("illusioner_hurt");
    public static final SoundEvent ILLUSIONER_DEATH = registerSoundEvent("illusioner_death");
    public static final SoundEvent ILLUSIONER_CLONE_DEATH = registerSoundEvent("illusioner_clone_death");
    public static final SoundEvent ILLUSIONER_ALL_DEATH = registerSoundEvent("illusioner_all_death");
    public static final SoundEvent ILLUSIONER_PREPARE_MIRROR = registerSoundEvent("illusioner_prepare_mirror");
    public static final SoundEvent ILLUSIONER_MIRROR = registerSoundEvent("illusioner_mirror");
    public static final SoundEvent ILLUSIONER_BLINDNESS = registerSoundEvent("illusioner_blindness");

    public static final SoundEvent WHISPERER_AMBIENT = registerSoundEvent("whisperer_ambient");
    public static final SoundEvent WHISPERER_HURT = registerSoundEvent("whisperer_hurt");
    public static final SoundEvent WHISPERER_DEATH = registerSoundEvent("whisperer_death");
    public static final SoundEvent WHISPERER_ATTACK = registerSoundEvent("whisperer_attack");
    public static final SoundEvent WHISPERER_SUMMON = registerSoundEvent("whisperer_summon");

    // NETHER

    // END

    // ARKONIA

    // BIRUNIA

    // ICEOLIA

    // IRIDIA

    // LUMINIA

    // STELLARIA

    // VELIA

    // ZYPHIA

    // XYOLGIA

    // FRACTURIA

    private static SoundEvent registerSoundEvent(String name)
    {
        Identifier id = Identifier.of(Fracturia.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds()
    {
        Fracturia.LOGGER.info("Registering Sounds for " + Fracturia.MOD_ID);
    }
}
