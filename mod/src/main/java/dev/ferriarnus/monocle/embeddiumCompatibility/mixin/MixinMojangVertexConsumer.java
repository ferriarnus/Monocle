package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import org.spongepowered.asm.mixin.Mixin;

//TODO stub class for facades, will fix when relevant
@Mixin(targets = "org/embeddedt/embeddium/impl/render/chunk/compile/buffers/BakedChunkModelBuilder$MojangVertexConsumer")
public class MixinMojangVertexConsumer implements BlockSensitiveBufferBuilder {

    @Override
    public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {

    }

    @Override
    public void overrideBlock(int block) {

    }

    @Override
    public void restoreBlock() {

    }

    @Override
    public void endBlock() {

    }

    @Override
    public void ignoreMidBlock(boolean b) {

    }
}
