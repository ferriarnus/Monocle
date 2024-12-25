package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.FAShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Config("forbidden_arcanus")
@Mixin(targets = "com/stal111/forbidden_arcanus/client/FAShaders")
public class MixinFAShaders {

    @Inject(method = "getRendertypeEntityFullbrightCutout", at = @At("HEAD"), cancellable = true)
    private static void replaceCutout(CallbackInfoReturnable<ShaderInstance> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(ModdedShaderPipeline.getShader(FAShaders.FULLBRIGHT_CUTOUT));
        }
    }

    @Inject(method = "getRendertypeEntityFullbrightTranslucent", at = @At("HEAD"), cancellable = true)
    private static void replaceTranslucent(CallbackInfoReturnable<ShaderInstance> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(ModdedShaderPipeline.getShader(FAShaders.FULLBRIGHT_TRANSLUCENT));
        }
    }
}
