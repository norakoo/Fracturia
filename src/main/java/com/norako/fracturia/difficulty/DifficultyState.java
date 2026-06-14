package com.norako.fracturia.difficulty;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class DifficultyState extends PersistentState {

    public static volatile Difficulty activeServerDifficulty = Difficulty.NONE;

    private static final Type<DifficultyState> TYPE = new Type<>(
            DifficultyState::new,
            DifficultyState::fromNbt,
            null
    );

    private Difficulty difficulty = Difficulty.NONE;

    public static DifficultyState get(ServerWorld world) {
        DifficultyState state = world.getPersistentStateManager().getOrCreate(TYPE, "fracturia_difficulty");
        activeServerDifficulty = state.difficulty;
        return state;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        activeServerDifficulty = difficulty;
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putString("difficulty", difficulty.getId());
        return nbt;
    }

    public static DifficultyState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        DifficultyState state = new DifficultyState();
        state.difficulty = Difficulty.fromId(nbt.getString("difficulty"));
        return state;
    }
}
