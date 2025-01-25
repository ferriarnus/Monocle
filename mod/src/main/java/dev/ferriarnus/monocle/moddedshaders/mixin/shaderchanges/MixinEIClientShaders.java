package dev.ferriarnus.monocle.moddedshaders.mixin.shaderchanges;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.mods.EIShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/swedz/extended_industrialization/EIClientShaders")
public class MixinEIClientShaders {

    @WrapOperation(method = "armorCutoutGlow", at = @At(value = "FIELD", target = "Lnet/swedz/extended_industrialization/EIClientShaders;ARMOR_CUTOUT_GLOW_INSTANCE:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapArmor(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ((ShaderRenderingPipeline)Iris.getPipelineManager().getPipelineNullable()).getShaderMap().getShader(ShaderKey.SHADOW_ENTITIES_CUTOUT);
        }
        return original.call();
    }
}
