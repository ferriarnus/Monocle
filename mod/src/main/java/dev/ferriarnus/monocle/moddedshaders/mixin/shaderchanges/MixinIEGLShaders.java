package dev.ferriarnus.monocle.moddedshaders.mixin.shaderchanges;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "blusunrize/immersiveengineering/client/utils/IEGLShaders")
public class MixinIEGLShaders {
    @Shadow
    private static ShaderInstance vboShader;

    @Inject(method = "getVboShader", at = @At("RETURN"), cancellable = true)
    private static void dontSwitch(CallbackInfoReturnable<ShaderInstance> cir) {
        if (Iris.isPackInUseQuick() && Iris.getIrisConfig().shouldAllowUnknownShaders() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(vboShader);
        }
    }
}
