package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.ferriarnus.monocle.moddedshaders.mods.MinecraftShaders;
import net.irisshaders.iris.gl.sampler.SamplerHolder;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.samplers.IrisSamplers;
import net.irisshaders.iris.targets.RenderTargets;
import net.minecraft.client.renderer.texture.AbstractTexture;
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

    @Inject(method = "addLevelSamplers", at = @At("TAIL"))
    private static void addSamper1(SamplerHolder samplers, WorldRenderingPipeline pipeline, AbstractTexture whitePixel, boolean hasTexture, boolean hasLightmap, boolean hasOverlay, CallbackInfo ci) {
        samplers.addDynamicSampler(() -> RenderSystem.getShaderTexture(0), "MonocleSampler0");
        samplers.addDynamicSampler(() -> RenderSystem.getShaderTexture(1), "MonocleSampler1");
        samplers.addDynamicSampler(() -> RenderSystem.getShaderTexture(2), "MonocleSampler2");
    }
}
