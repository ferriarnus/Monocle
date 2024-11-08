package dev.ferriarnus.monocle.moddedshaders.mixin;

import dev.ferriarnus.monocle.moddedshaders.impl.IrisRenderingPipelineExtension;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.targets.RenderTargets;
import org.checkerframework.checker.units.qual.A;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IrisRenderingPipeline.class)
public interface IrisRenderingPipelineAccessor extends IrisRenderingPipelineExtension {
    @Accessor
    RenderTargets getRenderTargets();
}
