package dev.ferriarnus.monocle.moddedshaders;

import dev.ferriarnus.monocle.moddedshaders.mods.FramedBlocksCamo;
import dev.ferriarnus.monocle.moddedshaders.mods.MekanismCoversCamo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.loading.LoadingModList;

public class CamoShaders {

    private static final boolean FRAMEDBLOCKS = LoadingModList.get().getModFileById("framedblocks") != null;
    private static final boolean MEK_COVERS = LoadingModList.get().getModFileById("mekanismcovers") != null;

    public static BlockState getBlockstate(BlockAndTintGetter slice, BlockPos pos, BlockState state) {
        BlockState camo = state;
        if (FRAMEDBLOCKS) {
            camo = FramedBlocksCamo.getBlockstate(slice, pos, camo);
        }
        if (MEK_COVERS) {
            camo = MekanismCoversCamo.getBlockstate(slice, pos, camo);
        }
        return camo.getAppearance(slice, pos, Direction.UP, null, null);

    }
}
