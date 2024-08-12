package dev.ferriarnus.monocle.embeddiumCompatibility.mixin.vertex_format;

import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertex_format.IrisModelVertexFormats;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
	@ModifyArg(method = "<init>", remap = false,
		at = @At(value = "INVOKE",
			target = "Lorg/embeddedt/embeddium/impl/render/chunk/DefaultChunkRenderer;<init>(Lorg/embeddedt/embeddium/impl/gl/device/RenderDevice;Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkVertexType;)V"))
	private ChunkVertexType iris$useExtendedVertexFormat$1(ChunkVertexType vertexType) {
		return WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}

	@ModifyArg(method = "<init>",
		at = @At(value = "INVOKE",
			target = "Lorg/embeddedt/embeddium/impl/render/chunk/compile/executor/ChunkBuilder;<init>(Lnet/minecraft/client/multiplayer/ClientLevel;Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkVertexType;)V"))
	private ChunkVertexType iris$useExtendedVertexFormat$2(ChunkVertexType vertexType) {
		return WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat() ? IrisModelVertexFormats.MODEL_VERTEX_XHFP : vertexType;
	}
}
