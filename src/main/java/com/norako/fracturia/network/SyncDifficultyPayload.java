package com.norako.fracturia.network;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.difficulty.Difficulty;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SyncDifficultyPayload(Difficulty difficulty) implements CustomPayload {

    public static final Id<SyncDifficultyPayload> ID =
            new Id<>(Identifier.of(Fracturia.MOD_ID, "sync_difficulty"));

    public static final PacketCodec<PacketByteBuf, SyncDifficultyPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeString(value.difficulty().getId()),
            buf -> new SyncDifficultyPayload(Difficulty.fromId(buf.readString()))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
