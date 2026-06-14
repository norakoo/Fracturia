package com.norako.fracturia.network;

import com.norako.fracturia.Fracturia;
import com.norako.fracturia.difficulty.Difficulty;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChangeDifficultyPayload(Difficulty difficulty) implements CustomPayload {

    public static final Id<ChangeDifficultyPayload> ID =
            new Id<>(Identifier.of(Fracturia.MOD_ID, "change_difficulty"));

    public static final PacketCodec<PacketByteBuf, ChangeDifficultyPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeString(value.difficulty().getId()),
            buf -> new ChangeDifficultyPayload(Difficulty.fromId(buf.readString()))
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
