package dev.ferriarnus.monocle.embeddiumCompatibility.mixin.vertex_format;

import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertex_format.IrisModelVertexFormats;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.embeddedt.embeddium.impl.render.chunk.region.RenderRegion;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkMeshFormats;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderRegion.DeviceResources.class)
public class MixinRenderRegionArenas {
	@Redirect(method = "<init>", remap = false,
		at = @At(value = "FIELD",
			target = "Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkMeshFormats;COMPACT:Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkVertexType;",
			remap = false))
	private ChunkVertexType iris$useExtendedStride() {
		return WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : ChunkMeshFormats.COMPACT;
	}
}
