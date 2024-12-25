package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.IEShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Config("immersiveengineering")
@Mixin(targets = "blusunrize/immersiveengineering/client/utils/IEGLShaders", remap = false)
public class MixinIEGLShaders {

    @Inject(method = "getPointShader", at = @At("RETURN"), cancellable = true)
    private static void wrapPoint(CallbackInfoReturnable<ShaderInstance> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(ModdedShaderPipeline.getShader(IEShaders.POINT));
            cir.cancel();
        }
    }

    @Inject(method = "getBlockFullbrightShader", at = @At("RETURN"), cancellable = true)
    private static void wrapFullBright(CallbackInfoReturnable<ShaderInstance> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(ModdedShaderPipeline.getShader(IEShaders.FULLBRIGHT));
            cir.cancel();
        }
    }

    @Inject(method = "getVboShader", at = @At("RETURN"), cancellable = true)
    private static void wrapVBO(CallbackInfoReturnable<ShaderInstance> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(ModdedShaderPipeline.getShader(IEShaders.VBO));
            cir.cancel();
        }
    }
}
