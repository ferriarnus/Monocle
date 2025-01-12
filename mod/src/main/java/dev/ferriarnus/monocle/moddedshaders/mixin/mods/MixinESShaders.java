package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.ESShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Config("eternal_starlight")
@Mixin(targets = "cn/leolezury/eternalstarlight/common/client/shader/ESShaders")
public class MixinESShaders {

    @WrapOperation(method = "getRenderTypeEclipse", at = @At(value = "FIELD", target = "Lcn/leolezury/eternalstarlight/common/client/shader/ESShaders;renderTypeEclipse:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapEclipse(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(ESShaders.ECLIPSE);
        }
        return original.call();
    }

    @WrapOperation(method = "getRenderTypeStarlightPortal", at = @At(value = "FIELD", target = "Lcn/leolezury/eternalstarlight/common/client/shader/ESShaders;renderTypeStarlightPortal:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapPortal(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(ESShaders.STARLIGHT_PORTAL);
        }
        return original.call();
    }
}
