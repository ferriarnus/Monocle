package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.BumbleShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Config("the_bumblezone")
@Mixin(targets = "com/telepathicgrunt/the_bumblezone/client/blockentityrenderer/EssenceBlockEntityRenderer", remap = false)
public class MixinEssenceBlockEntityRenderer {

    @Inject(method = "getType", at = @At("RETURN"), cancellable = true)
    private void wrapRenderType(CallbackInfoReturnable<RenderType> cir) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            cir.setReturnValue(BumbleShaders.ESSENCE_TYPE);
            cir.cancel();
        }
    }
}
