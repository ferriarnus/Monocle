package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkVertexType.class)
public interface MixinChunkVertexType extends net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType {
}
