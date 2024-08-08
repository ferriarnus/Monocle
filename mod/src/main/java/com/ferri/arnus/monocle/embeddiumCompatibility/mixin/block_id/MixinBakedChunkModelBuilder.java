package com.ferri.arnus.monocle.embeddiumCompatibility.mixin.block_id;

import com.ferri.arnus.monocle.embeddiumCompatibility.impl.block_context.BlockContextHolder;
import com.ferri.arnus.monocle.embeddiumCompatibility.impl.block_context.ContextAwareVertexWriter;
import org.embeddedt.embeddium.impl.render.chunk.compile.buffers.BakedChunkModelBuilder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BakedChunkModelBuilder.class)
public class MixinBakedChunkModelBuilder implements ContextAwareVertexWriter {

	@Shadow
	@Final
	private ChunkMeshBufferBuilder[] vertexBuffers;

	@Override
	public void iris$setContextHolder(BlockContextHolder holder) {
		for (ChunkMeshBufferBuilder builder : this.vertexBuffers) {
			((ContextAwareVertexWriter) builder).iris$setContextHolder(holder);
		}
	}

	@Override
	public void flipUpcomingQuadNormal() {
		for (ChunkMeshBufferBuilder builder : this.vertexBuffers) {
			((ContextAwareVertexWriter) builder).flipUpcomingQuadNormal();
		}
	}
}
