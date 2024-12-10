package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import dev.ferriarnus.monocle.moddedshaders.mods.DireShaders;
import dev.ferriarnus.monocle.moddedshaders.mods.MinecraftShaders;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.targets.RenderTargets;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IrisSamplers.class)
public class MixinIrisSamplers {

    @Inject(method = "addWorldDepthSamplers", at = @At("TAIL"))
    private static void addDepth(SamplerHolder samplers, RenderTargets renderTargets, CallbackInfo ci) {
        if (MinecraftShaders.needsDepth()) {
            samplers.addDynamicSampler(MinecraftShaders::getDepthId, "MonocleDepth");
        }
    }
}
