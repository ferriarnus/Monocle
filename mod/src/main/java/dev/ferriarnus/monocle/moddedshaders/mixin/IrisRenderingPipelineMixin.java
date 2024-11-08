package dev.ferriarnus.monocle.moddedshaders.mixin;

import dev.ferriarnus.monocle.moddedshaders.ModdedShaderCreator;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.impl.WorldRenderingPipelineExtension;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisRenderingPipeline.class)
public class IrisRenderingPipelineMixin implements WorldRenderingPipelineExtension {
    @Unique
    private ProgramSet programSet;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void captureSet(ProgramSet programSet, CallbackInfo ci) {
        this.programSet = programSet;
    }

    @Unique
    public ProgramSet getProgramSet() {
        return programSet;
    }

    @Inject(method = "destroyShaders", at = @At("TAIL"))
    public void destroyModdedShaders(CallbackInfo ci) {
        ModdedShaderPipeline.destroyShaders();
    }
}
