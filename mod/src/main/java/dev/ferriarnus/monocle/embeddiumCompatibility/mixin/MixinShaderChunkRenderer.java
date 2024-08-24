package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumShader;
import dev.ferriarnus.monocle.irisCompatibility.impl.WorldRenderingPipelineExtension;
import dev.ferriarnus.monocle.irisCompatibility.mixin.WorldRenderingPipelineMixin;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.SodiumShader;
import org.embeddedt.embeddium.impl.gl.shader.GlProgram;
import org.embeddedt.embeddium.impl.render.chunk.ShaderChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderInterface;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderOptions;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShaderChunkRenderer.class, remap = false)
public abstract class MixinShaderChunkRenderer {
	@Shadow
	protected abstract GlProgram<ChunkShaderInterface> compileProgram(ChunkShaderOptions options);

	@Shadow
	protected GlProgram<ChunkShaderInterface> activeProgram;

	@Redirect(method = "begin", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/chunk/ShaderChunkRenderer;compileProgram(Lorg/embeddedt/embeddium/impl/render/chunk/shader/ChunkShaderOptions;)Lorg/embeddedt/embeddium/impl/gl/shader/GlProgram;"))
	private GlProgram<ChunkShaderInterface> redirectIrisProgram(ShaderChunkRenderer instance, ChunkShaderOptions options, TerrainRenderPass pass) {
		WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();

		GlProgram<ChunkShaderInterface> program = null;

		if (pipeline instanceof WorldRenderingPipelineExtension irisRenderingPipeline) {
			irisRenderingPipeline.getEmbeddiumPrograms().getFramebuffer(pass).bind();
			program = irisRenderingPipeline.getEmbeddiumPrograms().getProgram(pass);
		}

		if (program == null) {
			return this.compileProgram(options);
		}

		return program;
	}

	@Inject(method = "end", at = @At("HEAD"))
	private void end(TerrainRenderPass pass, CallbackInfo ci) {
		if (this.activeProgram.getInterface() instanceof EmbeddiumShader shader) {
			shader.resetState();
		}
	}
}
