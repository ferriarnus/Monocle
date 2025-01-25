package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.sodium.terrain.BlockContextHolder;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import org.embeddedt.embeddium.impl.render.chunk.compile.buffers.BakedChunkModelBuilder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BakedChunkModelBuilder.class, remap = false)
public class MixinBakedChunkModelBuilder implements BlockSensitiveBufferBuilder {
    @Unique
    private final BlockContextHolder contextHolder = new BlockContextHolder();

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void setupContextHolder(ChunkMeshBufferBuilder[] vertexBuffers, boolean splitBySide, CallbackInfo ci) {
        for (ChunkMeshBufferBuilder vertexBuffer : vertexBuffers) {
            ((VertexEncoderInterface) vertexBuffer).iris$setContextHolder(contextHolder);
        }
    }

    @Override
    public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
        contextHolder.setBlockData(block, renderType, blockEmission, localPosX, localPosY, localPosZ);
    }

    @Override
    public void overrideBlock(int block) {
        contextHolder.overrideBlock(block);
    }

    @Override
    public void restoreBlock() {
        contextHolder.restoreBlock();
    }

    @Override
    public void endBlock() {
        contextHolder.setBlockData(0, (byte) 0, (byte) 0, 0, 0, 0);
    }

    @Override
    public void ignoreMidBlock(boolean b) {
        contextHolder.setIgnoreMidBlock(b);
    }
}
