package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.Uniform;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.XYShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Config("xycraft")
@Mixin(targets = "tv/soaryn/xycraft/machines/client/render/XynergyGraphRenderer", remap = false)
public class MixinXynergyGraphRenderer {

    @WrapOperation(method = "setShaderValues", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ShaderInstance;getUniform(Ljava/lang/String;)Lcom/mojang/blaze3d/shaders/Uniform;"))
    private static Uniform wrapShader(ShaderInstance instance, String name, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            if (instance.getName().equals(XYShaders.LASER_NODE.toString())) {
                return ModdedShaderPipeline.getShader(XYShaders.LASER_NODE).getUniform(name);
            }
            if (instance.getName().equals(XYShaders.LASER.toString())) {
                return ModdedShaderPipeline.getShader(XYShaders.LASER).getUniform(name);
            }
        }
        return original.call(instance, name);
    }

}
