package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.mods.MinecraftShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @WrapOperation(method = "getRendertypeEndPortalShader", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;rendertypeEndPortalShader:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance iris$overrideEndPortalShader(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(MinecraftShaders.END_PORTAL);
        }
        return original.call();
    }

    @WrapOperation(method = "getRendertypeEndGatewayShader", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;rendertypeEndGatewayShader:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance iris$overrideEndGatewayShader(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(MinecraftShaders.END_GATEWAY);
        }
        return original.call();
    }
}
