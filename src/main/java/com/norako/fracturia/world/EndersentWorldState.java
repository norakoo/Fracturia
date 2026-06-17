package com.norako.fracturia.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class EndersentWorldState extends PersistentState {

    public enum Variant {
        BINDING, BLIGHT, RAVENOUS, REAPING, SAVAGE, SPIKED
    }

    private final boolean[] killed = new boolean[6];

    public static final Type<EndersentWorldState> TYPE = new Type<>(
            EndersentWorldState::new,
            EndersentWorldState::fromNbt,
            null
    );

    public static EndersentWorldState get(MinecraftServer server) {
        PersistentStateManager manager = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return manager.getOrCreate(TYPE, "endersent_kills");
    }

    public boolean isKilled(Variant variant) {
        return killed[variant.ordinal()];
    }

    public void setKilled(Variant variant) {
        killed[variant.ordinal()] = true;
        markDirty();
    }

    private boolean portalRoomVisited = false;

    public boolean isPortalRoomVisited() { return portalRoomVisited; }
    public void setPortalRoomVisited() { portalRoomVisited = true; markDirty(); }

    public static EndersentWorldState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        EndersentWorldState state = new EndersentWorldState();
        for (Variant v : Variant.values()) {
            state.killed[v.ordinal()] = nbt.getBoolean(v.name().toLowerCase());
            state.spawned[v.ordinal()] = nbt.getBoolean("spawned_" + v.name().toLowerCase());
            state.portalRoomVisited = nbt.getBoolean("portal_room_visited");
        }
        return state;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        for (Variant v : Variant.values()) {
            nbt.putBoolean(v.name().toLowerCase(), (killed[v.ordinal()]));
            nbt.putBoolean("spawned_" + v.name().toLowerCase(), spawned[v.ordinal()]);

            nbt.putBoolean("portal_room_visited", portalRoomVisited);
        }
        return nbt;
    }

    private final boolean[] spawned = new boolean[6];

    public boolean isSpawned(Variant variant) {
        return spawned[variant.ordinal()];
    }

    public void setSpawned(Variant variant) {
        spawned[variant.ordinal()] = true;
        markDirty();
    }

    public void clearSpawned(Variant variant) {
        spawned[variant.ordinal()] = false;
        markDirty();
    }
}
