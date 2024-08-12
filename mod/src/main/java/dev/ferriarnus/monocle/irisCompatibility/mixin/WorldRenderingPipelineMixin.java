package dev.ferriarnus.monocle.irisCompatibility.mixin;

import dev.ferriarnus.monocle.irisCompatibility.impl.WorldRenderingPipelineExtension;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldRenderingPipeline.class)
public interface WorldRenderingPipelineMixin extends WorldRenderingPipelineExtension {
}
