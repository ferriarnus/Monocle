package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.mods.MinecraftShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    @Inject(method = "getRendertypeEndPortalShader", at = @At("HEAD"), cancellable = true)
    private static void iris$overrideEndPortalShader(CallbackInfoReturnable<ShaderInstance> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            monocle$override(MinecraftShaders.END_PORTAL, cir);
        }
    }

    @Inject(method = "getRendertypeEndGatewayShader", at = @At("HEAD"), cancellable = true)
    private static void iris$overrideEndGatewayShader(CallbackInfoReturnable<ShaderInstance> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            monocle$override(MinecraftShaders.END_GATEWAY, cir);
        }
    }

    @Unique
    private static void monocle$override(ResourceLocation key, CallbackInfoReturnable<ShaderInstance> cir) {
        WorldRenderingPipeline pipeline = Iris.getPipelineManager().getPipelineNullable();
        if (pipeline instanceof ShaderRenderingPipeline) {
            ShaderInstance override = ModdedShaderPipeline.getShader(key);
            if (override != null) {
                cir.setReturnValue(override);
                cir.cancel();
            }
        }
    }
}
