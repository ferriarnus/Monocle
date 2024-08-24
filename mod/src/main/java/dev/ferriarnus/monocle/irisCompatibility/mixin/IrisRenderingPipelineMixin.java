package dev.ferriarnus.monocle.irisCompatibility.mixin;

import com.google.common.collect.ImmutableSet;
import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumPrograms;
import dev.ferriarnus.monocle.irisCompatibility.impl.WorldRenderingPipelineExtension;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.SodiumPrograms;
import net.irisshaders.iris.shaderpack.programs.ProgramFallbackResolver;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.IntFunction;
import java.util.function.Supplier;

@Mixin(IrisRenderingPipeline.class)
public abstract class IrisRenderingPipelineMixin implements WorldRenderingPipeline, WorldRenderingPipelineExtension {

    @Unique
    private EmbeddiumPrograms embeddiumPrograms;

    @Redirect(at = @At(value = "NEW", target ="(Lnet/irisshaders/iris/pipeline/IrisRenderingPipeline;Lnet/irisshaders/iris/shaderpack/programs/ProgramSet;Lnet/irisshaders/iris/shaderpack/programs/ProgramFallbackResolver;Lnet/irisshaders/iris/targets/RenderTargets;Ljava/util/function/Supplier;Lnet/irisshaders/iris/uniforms/custom/CustomUniforms;)Lnet/irisshaders/iris/pipeline/programs/SodiumPrograms;"), method = "<init>", remap = false)
    private SodiumPrograms makePipeline(IrisRenderingPipeline flipState, ProgramSet framebuffer, ProgramFallbackResolver alphaTest, RenderTargets transformed, Supplier shader, CustomUniforms pass) {
        embeddiumPrograms =  new EmbeddiumPrograms(flipState, framebuffer, alphaTest, transformed, shader, pass);
        return new SodiumPrograms(flipState, framebuffer, alphaTest, transformed, shader, pass);
    }

    @Override
    public EmbeddiumPrograms getEmbeddiumPrograms() {
        return embeddiumPrograms;
    }
}
