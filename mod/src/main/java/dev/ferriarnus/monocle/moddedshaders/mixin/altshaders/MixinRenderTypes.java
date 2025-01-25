package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.COShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Config("creeperoverhaul")
@Mixin(targets = "tech/thatgravyboat/creeperoverhaul/client/RenderTypes")
public class MixinRenderTypes {

    @WrapOperation(method = "lambda$static$0", at = @At(value = "FIELD", target = "Ltech/thatgravyboat/creeperoverhaul/client/RenderTypes;energySwirlShader:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance replace(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(COShaders.ENERGY_SWIRL_RENDERTYPE);
        }
        return original.call();
    }
}
