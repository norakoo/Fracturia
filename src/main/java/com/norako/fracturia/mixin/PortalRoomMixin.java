package com.norako.fracturia.mixin;

import net.minecraft.block.Blocks;
import net.minecraft.block.EndPortalFrameBlock;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

@Mixin(targets = "net.minecraft.structure.StrongholdGenerator$PortalRoom")
public class PortalRoomMixin {

    @Inject(method = "generate", at = @At("RETURN"))
    private void fracturia_fixPortalEyes(StructureWorldAccess world, StructureAccessor structureAccessor,
                                         ChunkGenerator chunkGenerator, Random random,
                                         BlockBox chunkBox, ChunkPos chunkPos, BlockPos pivot,
                                         CallbackInfo ci) {
        BlockBox box = ((StructurePiece)(Object)this).getBoundingBox();

        List<BlockPos> frames = new ArrayList<>();
        for (int x = box.getMinX(); x <= box.getMaxX(); x++) {
            for (int y = box.getMinY(); y <= box.getMaxY(); y++) {
                for (int z = box.getMinZ(); z <= box.getMaxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (world.getBlockState(pos).isOf(Blocks.END_PORTAL_FRAME)) {
                        frames.add(pos);
                    }
                }
            }
        }

        if (frames.size() != 12) return;

        for(int i = frames.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            BlockPos temp = frames.get(i);
            frames.set(i, frames.get(j));
            frames.set(j, temp);
        }

        for (int i = 0; i < frames.size(); i++) {
            BlockPos pos = frames.get(i);
            world.setBlockState(pos,
                    world.getBlockState(pos).with(EndPortalFrameBlock.EYE, i < 6), 3);
        }
    }
}
