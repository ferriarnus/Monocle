package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.ArsShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Config("arsnouveau")
@Mixin(targets = "com/hollingsworth/arsnouveau/client/registry/ShaderRegistry")
public class MixinShaderRegistry {

    @WrapOperation(method = "lambda$static$0", at = @At(value = "FIELD", target = "Lcom/hollingsworth/arsnouveau/client/ClientInfo;skyShader:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance replaceSky(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(ArsShaders.SKY);
        }
        return original.call();
    }

    @WrapOperation(method = "lambda$static$3", at = @At(value = "FIELD", target = "Lcom/hollingsworth/arsnouveau/client/ClientInfo;rainbowShader:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance replaceRainbow(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(ArsShaders.RAINBOW_ENTITY);
        }
        return original.call();
    }

    @WrapOperation(method = "lambda$static$5", at = @At(value = "FIELD", target = "Lcom/hollingsworth/arsnouveau/client/ClientInfo;blameShader:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance replaceBlame(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(ArsShaders.BLAMED_ENTITY);
        }
        return original.call();
    }

}
