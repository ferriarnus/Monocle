package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.IEShaders;
import dev.ferriarnus.monocle.moddedshaders.mods.MekShaders;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.pipeline.programs.ShaderMap;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Config("mekanism")
@Mixin(ShaderAccess.class)
public class ShaderAccessMixin {

    @Redirect(method = "getMekasuitShader", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pipeline/programs/ShaderMap;getShader(Lnet/irisshaders/iris/pipeline/programs/ShaderKey;)Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance replaceMek(ShaderMap instance, ShaderKey id) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return instance.getShader(id);
        }
        return ModdedShaderPipeline.getShader(MekShaders.MEKASUIT);
    }

    @Redirect(method = "getMekanismFlameShader", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pipeline/programs/ShaderMap;getShader(Lnet/irisshaders/iris/pipeline/programs/ShaderKey;)Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance replaceFlame(ShaderMap instance, ShaderKey id) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return instance.getShader(id);
        }
        return ModdedShaderPipeline.getShader(MekShaders.FLAME);
    }

    @Redirect(method = "getSPSShader", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pipeline/programs/ShaderMap;getShader(Lnet/irisshaders/iris/pipeline/programs/ShaderKey;)Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance replaceSPS(ShaderMap instance, ShaderKey id) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return instance.getShader(id);
        }
        return ModdedShaderPipeline.getShader(MekShaders.SPS);
    }

    @Redirect(method = "getIEVBOShader", at = @At(value = "INVOKE", target = "Lnet/irisshaders/iris/pipeline/programs/ShaderMap;getShader(Lnet/irisshaders/iris/pipeline/programs/ShaderKey;)Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance replaceVBO(ShaderMap instance, ShaderKey id) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return instance.getShader(id);
        }
        return ModdedShaderPipeline.getShader(IEShaders.VBO);
    }
}
