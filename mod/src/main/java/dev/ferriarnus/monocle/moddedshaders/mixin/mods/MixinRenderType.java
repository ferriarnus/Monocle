package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import dev.ferriarnus.monocle.moddedshaders.mods.MinecraftShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderType.class)
public class MixinRenderType {

    @Inject(method = "endPortal", at = @At("TAIL"), cancellable = true)
    private static void replaceEndPortal(CallbackInfoReturnable<RenderType> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(MinecraftShaders.END_PORTAL_TYPE);
        }
    }

    @Inject(method = "endGateway", at = @At("TAIL"), cancellable = true)
    private static void replaceEndGateway(CallbackInfoReturnable<RenderType> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(MinecraftShaders.END_GATEWAY_TYPE);
        }
    }
}
