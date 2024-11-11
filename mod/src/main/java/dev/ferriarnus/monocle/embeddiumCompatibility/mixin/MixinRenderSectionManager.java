package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.embeddedt.embeddium.impl.gui.EmbeddiumOptions;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
	@ModifyArg(method = "<init>", remap = false,
		at = @At(value = "INVOKE",
			target = "Lorg/embeddedt/embeddium/impl/render/chunk/DefaultChunkRenderer;<init>(Lorg/embeddedt/embeddium/impl/gl/device/RenderDevice;Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkVertexType;)V"))
	private ChunkVertexType iris$useExtendedVertexFormat$1(ChunkVertexType vertexType) {
		return IrisApi.getInstance().isShaderPackInUse() ? (ChunkVertexType) WorldRenderingSettings.INSTANCE.getVertexFormat() : vertexType;
	}

	@ModifyArg(method = "<init>",
		at = @At(value = "INVOKE",
			target = "Lorg/embeddedt/embeddium/impl/render/chunk/compile/executor/ChunkBuilder;<init>(Lnet/minecraft/client/multiplayer/ClientLevel;Lorg/embeddedt/embeddium/impl/render/chunk/vertex/format/ChunkVertexType;)V"))
	private ChunkVertexType iris$useExtendedVertexFormat$2(ChunkVertexType vertexType) {
		return IrisApi.getInstance().isShaderPackInUse() ? (ChunkVertexType) WorldRenderingSettings.INSTANCE.getVertexFormat() : vertexType;
	}

	@Redirect(method = "getSearchDistance", remap = false,
		at = @At(value = "FIELD",
			target = "Lorg/embeddedt/embeddium/impl/gui/EmbeddiumOptions$PerformanceSettings;useFogOcclusion:Z",
			remap = false))
	private boolean iris$disableFogOcclusion(EmbeddiumOptions.PerformanceSettings settings) {
		if (Iris.getCurrentPack().isPresent()) {
			return false;
		} else {
			return settings.useFogOcclusion;
		}
	}
}
