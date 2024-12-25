package dev.ferriarnus.monocle.moddedshaders.mods;

import dev.lucaargolo.mekanismcovers.mixed.TileEntityTransmitterMixed;
import mekanism.common.block.transmitter.BlockTransmitter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class MekanismCoversCamo {

    public static BlockState getBlockstate(BlockAndTintGetter slice, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof BlockTransmitter && slice.getBlockEntity(pos) instanceof TileEntityTransmitterMixed transmitter) {
            return transmitter.mekanism_covers$getCoverState() == null ? state : transmitter.mekanism_covers$getCoverState();
        }
        return state;
    }
}
