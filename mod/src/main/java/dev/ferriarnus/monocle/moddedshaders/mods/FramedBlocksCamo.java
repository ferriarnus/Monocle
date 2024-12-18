package dev.ferriarnus.monocle.moddedshaders.mods;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import xfacthd.framedblocks.api.block.IFramedBlock;
import xfacthd.framedblocks.api.block.blockentity.FramedBlockEntity;

public class FramedBlocksCamo {

    public static BlockState getBlockstate(BlockAndTintGetter slice, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof IFramedBlock && slice.getBlockEntity(pos) instanceof FramedBlockEntity framedBlockEntity) {
            BlockState camo = framedBlockEntity.getCamo().getContent().getAsBlockState();
            if (!camo.isAir()) {
                return camo;
            }
        }
        return state;
    }
}
