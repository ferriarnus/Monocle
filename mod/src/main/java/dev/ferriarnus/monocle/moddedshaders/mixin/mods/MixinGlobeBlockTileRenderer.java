package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.Uniform;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.mods.SuppShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Function;

@Mixin(targets = "net/mehvahdjukaar/supplementaries/client/renderers/tiles/GlobeBlockTileRenderer")
public class MixinGlobeBlockTileRenderer {

    @WrapOperation(method = "renderGlobe", at = @At(value = "FIELD", target = "Lnet/mehvahdjukaar/supplementaries/client/renderers/NoiseRenderType;STATIC_NOISE:Ljava/util/function/Function;"))
    private Function<ResourceLocation, RenderType> wrapRenderType(Operation<Function<ResourceLocation, RenderType>> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return SuppShaders.STATIC_NOISE;
        }
        return original.call();
    }

    @WrapOperation(method = "renderGlobe", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderInstance;getUniform(Ljava/lang/String;)Lcom/mojang/blaze3d/shaders/Uniform;"))
    private Uniform wrapRenderType(ShaderInstance instance, String name, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(SuppShaders.STATIC).getUniform(name);
        }
        return original.call(instance, name);
    }
}
