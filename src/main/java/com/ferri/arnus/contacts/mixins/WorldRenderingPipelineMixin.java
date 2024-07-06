package com.ferri.arnus.contacts.mixins;

import com.ferri.arnus.contacts.irisCompatibility.WorldRenderingPipelineExtension;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WorldRenderingPipeline.class)
public interface WorldRenderingPipelineMixin extends WorldRenderingPipelineExtension {
}
