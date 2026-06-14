package com.norako.fracturia.difficulty;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class FracturiaDifficultyState extends PersistentState {

    // Static cache accessible from anywhere server-side (ore feature, damage mixins, etc.)
    public static volatile FracturiaDifficulty activeServerDifficulty = FracturiaDifficulty.NONE;

    private static final Type<FracturiaDifficultyState> TYPE = new Type<>(
        FracturiaDifficultyState::new,
        FracturiaDifficultyState::fromNbt,
        null
    );

    private FracturiaDifficulty difficulty = FracturiaDifficulty.NONE;
    private boolean initialized = false;

    public static FracturiaDifficultyState get(ServerWorld world) {
        FracturiaDifficultyState state = world.getPersistentStateManager().getOrCreate(TYPE, "fracturia_difficulty");
        activeServerDifficulty = state.difficulty;
        return state;
    }

    public FracturiaDifficulty getDifficulty() { return difficulty; }
    public boolean isInitialized() { return initialized; }

    public void initialize(FracturiaDifficulty difficulty) {
        this.difficulty = difficulty;
        activeServerDifficulty = difficulty;
        this.initialized = true;
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putString("difficulty", difficulty.getId());
        nbt.putBoolean("initialized", initialized);
        return nbt;
    }

    public static FracturiaDifficultyState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        FracturiaDifficultyState state = new FracturiaDifficultyState();
        state.difficulty = FracturiaDifficulty.fromId(nbt.getString("difficulty"));
        state.initialized = nbt.getBoolean("initialized");
        return state;
    }
}
