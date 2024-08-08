package com.ferri.arnus.monocle.embeddiumCompatibility.mixin.block_id;

import com.ferri.arnus.monocle.embeddiumCompatibility.impl.block_context.BlockContextHolder;
import com.ferri.arnus.monocle.embeddiumCompatibility.impl.block_context.ContextAwareVertexWriter;
import org.embeddedt.embeddium.impl.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexEncoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkMeshBufferBuilder.class)
public class MixinChunkVertexBufferBuilder implements ContextAwareVertexWriter {
	@Shadow
	@Final
	private ChunkVertexEncoder encoder;

	@Override
	public void iris$setContextHolder(BlockContextHolder holder) {
		if (encoder instanceof ContextAwareVertexWriter) {
			((ContextAwareVertexWriter) encoder).iris$setContextHolder(holder);
		}
	}

	@Override
	public void flipUpcomingQuadNormal() {
		if (encoder instanceof ContextAwareVertexWriter) {
			((ContextAwareVertexWriter) encoder).flipUpcomingQuadNormal();
		}
	}
}
