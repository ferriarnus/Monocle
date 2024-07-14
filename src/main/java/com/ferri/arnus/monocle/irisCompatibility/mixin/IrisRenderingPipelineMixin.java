package com.ferri.arnus.monocle.irisCompatibility.mixin;

import com.ferri.arnus.monocle.irisCompatibility.impl.EmbeddiumTerrainPipeline;
import com.ferri.arnus.monocle.irisCompatibility.impl.WorldRenderingPipelineExtension;
import com.google.common.collect.ImmutableSet;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.SodiumTerrainPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramFallbackResolver;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.IntFunction;

@Mixin(IrisRenderingPipeline.class)
public abstract class IrisRenderingPipelineMixin implements WorldRenderingPipeline, WorldRenderingPipelineExtension {

    @Unique
    private EmbeddiumTerrainPipeline embeddiumTerrainPipeline;

    @Redirect(at = @At(value = "NEW", target = "(Lnet/irisshaders/iris/pipeline/WorldRenderingPipeline;Lnet/irisshaders/iris/shaderpack/programs/ProgramFallbackResolver;Lnet/irisshaders/iris/shaderpack/programs/ProgramSet;Ljava/util/function/IntFunction;Ljava/util/function/IntFunction;Ljava/util/function/IntFunction;Ljava/util/function/IntFunction;Lnet/irisshaders/iris/targets/RenderTargets;Lcom/google/common/collect/ImmutableSet;Lcom/google/common/collect/ImmutableSet;Lnet/irisshaders/iris/gl/framebuffer/GlFramebuffer;Lnet/irisshaders/iris/uniforms/custom/CustomUniforms;)Lnet/irisshaders/iris/pipeline/SodiumTerrainPipeline;"), method = "<init>", remap = false)
    private SodiumTerrainPipeline makePipeline(WorldRenderingPipeline parent, ProgramFallbackResolver resolver, ProgramSet programSet, IntFunction createTerrainSamplers, IntFunction createShadowSamplers, IntFunction createTerrainImages, IntFunction createShadowImages, RenderTargets targets, ImmutableSet flippedAfterPrepare, ImmutableSet flippedAfterTranslucent, GlFramebuffer shadowFramebuffer, CustomUniforms customUniforms) {
        embeddiumTerrainPipeline =  new EmbeddiumTerrainPipeline(parent, resolver, programSet, createTerrainSamplers, createShadowSamplers, createTerrainImages, createShadowImages, targets, flippedAfterPrepare, flippedAfterTranslucent, shadowFramebuffer, customUniforms);
        return new SodiumTerrainPipeline(parent, resolver, programSet, createTerrainSamplers, createShadowSamplers, createTerrainImages, createShadowImages, targets, flippedAfterPrepare, flippedAfterTranslucent, shadowFramebuffer, customUniforms);
    }

    @Override
    public EmbeddiumTerrainPipeline getEmbeddiumTerrainPipeline() {
        return embeddiumTerrainPipeline;
    }
}
