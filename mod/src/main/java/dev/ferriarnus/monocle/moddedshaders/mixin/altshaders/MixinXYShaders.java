package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.XYShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Config("xycraft")
@Mixin(targets = "tv/soaryn/xycraft/core/client/shader/CoreRenderTypes", remap = false)
public interface MixinXYShaders {

    @Inject(method = "laserNode", at = @At("HEAD"), cancellable = true)
    private static void extendedLaserNode(ResourceLocation texture, CallbackInfoReturnable<RenderType> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(XYShaders.LaserNode.apply(texture));
            cir.cancel();
        }
    }

    @Inject(method = "laser", at = @At("HEAD"), cancellable = true)
    private static void extendedLaser(ResourceLocation texture, CallbackInfoReturnable<RenderType> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(XYShaders.Laser.apply(texture));
            cir.cancel();
        }
    }

    @Inject(method = "icosphere", at = @At("HEAD"), cancellable = true)
    private static void extendedIcosphere(ResourceLocation texture, CallbackInfoReturnable<RenderType> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(XYShaders.icosphere.apply(texture));
            cir.cancel();
        }
    }
}
