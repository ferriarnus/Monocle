package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildBuffers;
import org.embeddedt.embeddium.impl.render.chunk.compile.buffers.BakedChunkModelBuilder;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ChunkBuildBuffers.class, remap = false)
public class MixinChunkBuildBuffers implements BlockSensitiveBufferBuilder {
	@Shadow
	@Final
	private Reference2ReferenceOpenHashMap<TerrainRenderPass, BakedChunkModelBuilder> builders;

	@Override
	public void beginBlock(int block, byte renderType, byte blockEmission, int localPosX, int localPosY, int localPosZ) {
		for (BakedChunkModelBuilder value : builders.values()) {
			((BlockSensitiveBufferBuilder) value).beginBlock(block, renderType, blockEmission, localPosX, localPosY, localPosZ);
		}
	}

	@Override
	public void overrideBlock(int block) {
		for (BakedChunkModelBuilder value : builders.values()) {
			((BlockSensitiveBufferBuilder) value).overrideBlock(block);
		}
	}

	@Override
	public void restoreBlock() {
		for (BakedChunkModelBuilder value : builders.values()) {
			((BlockSensitiveBufferBuilder) value).restoreBlock();
		}
	}

	@Override
	public void endBlock() {
		for (BakedChunkModelBuilder value : builders.values()) {
			((BlockSensitiveBufferBuilder) value).endBlock();
		}
	}

	@Override
	public void ignoreMidBlock(boolean b) {
		for (BakedChunkModelBuilder value : builders.values()) {
			((BlockSensitiveBufferBuilder) value).ignoreMidBlock(b);
		}
	}
}
