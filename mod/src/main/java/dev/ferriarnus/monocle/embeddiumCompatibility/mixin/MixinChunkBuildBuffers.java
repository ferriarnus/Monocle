package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.sodium.terrain.BlockContextHolder;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildBuffers;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.embeddedt.embeddium.impl.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChunkBuildBuffers.class, remap = false)
public class MixinChunkBuildBuffers implements BlockSensitiveBufferBuilder {
	@Unique
	private final BlockContextHolder contextHolder = new BlockContextHolder();

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Reference2ReferenceOpenHashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private void setupContextHolder(ChunkVertexType vertexType, CallbackInfo ci, @Local TerrainRenderPass pass, @Local ChunkMeshBufferBuilder[] vertexBuffers) {
		for (ChunkMeshBufferBuilder vertexBuffer : vertexBuffers) {
			((VertexEncoderInterface) vertexBuffer).iris$setContextHolder(contextHolder);
		}
	}

	@Override
	public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
		contextHolder.setBlockData(block, renderType, blockEmission, localPosX, localPosY, localPosZ);
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
