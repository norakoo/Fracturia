package com.norako.fracturia.network;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.difficulty.FracturiaDifficulty;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncDifficultyPayload(FracturiaDifficulty difficulty, boolean initialized) implements CustomPayload {

    public static final Id<SyncDifficultyPayload> ID =
        new Id<>(Identifier.of(Fracturia.MOD_ID, "sync_difficulty"));

    public static final PacketCodec<PacketByteBuf, SyncDifficultyPayload> CODEC = PacketCodec.of(
        (value, buf) -> {
            buf.writeString(value.difficulty().getId());
            buf.writeBoolean(value.initialized());
        },
        buf -> new SyncDifficultyPayload(FracturiaDifficulty.fromId(buf.readString()), buf.readBoolean())
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
