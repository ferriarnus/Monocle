package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.mods.CaxtonShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "xyz/flirora/caxton/render/CaxtonTextRenderLayers$RenderLayerFunctions")
public class MixinCaxtonTextRenderLayers {

    @WrapOperation(method = {"lambda$ofText$0", "lambda$ofTextOffset$2"}, at = @At(value = "FIELD", target = "Lxyz/flirora/caxton/render/CaxtonShaders;caxtonTextShader:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapText(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(CaxtonShaders.TEXT);
        }
        return original.call();
    }

    @WrapOperation(method = "lambda$ofTextSeeThrough$1", at = @At(value = "FIELD", target = "Lxyz/flirora/caxton/render/CaxtonShaders;caxtonTextSeeThroughShader:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapTextSeeThrough(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(CaxtonShaders.TEXT_SEE_THROUGH);
        }
        return original.call();
    }

    @WrapOperation(method = "lambda$ofTextOutline$3", at = @At(value = "FIELD", target = "Lxyz/flirora/caxton/render/CaxtonShaders;caxtonTextOutlineShader:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapTextOutline(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(CaxtonShaders.TEXT_OUTLINE);
        }
        return original.call();
    }
}