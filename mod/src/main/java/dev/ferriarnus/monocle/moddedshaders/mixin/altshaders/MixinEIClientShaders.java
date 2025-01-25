package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.EIShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Config("extended_industrialization")
@Mixin(targets = "net/swedz/extended_industrialization/EIClientShaders")
public class MixinEIClientShaders {

    @WrapOperation(method = "teslaArc", at = @At(value = "FIELD", target = "Lnet/swedz/extended_industrialization/EIClientShaders;TESLA_ARC_INSTANCE:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapArc(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(EIShaders.ARC);
        }
        return original.call();
    }

    @WrapOperation(method = "teslaPlasma", at = @At(value = "FIELD", target = "Lnet/swedz/extended_industrialization/EIClientShaders;TESLA_PLASMA_INSTANCE:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapPlasma(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return ModdedShaderPipeline.getShader(EIShaders.PLASMA);
        }
        return original.call();
    }

    @WrapOperation(method = "nanoQuantum", at = @At(value = "FIELD", target = "Lnet/swedz/extended_industrialization/EIClientShaders;NANO_QUANTUM_INSTANCE:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapQuantum(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel) {
            return ModdedShaderPipeline.getShader(EIShaders.QUANTUM);
        }
        return original.call();
    }

    @WrapOperation(method = "armorCutoutGlow", at = @At(value = "FIELD", target = "Lnet/swedz/extended_industrialization/EIClientShaders;ARMOR_CUTOUT_GLOW_INSTANCE:Lnet/minecraft/client/renderer/ShaderInstance;"))
    private static ShaderInstance wrapArmor(Operation<ShaderInstance> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel) {
            return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? ((ShaderRenderingPipeline)Iris.getPipelineManager().getPipelineNullable()).getShaderMap().getShader(ShaderKey.SHADOW_ENTITIES_CUTOUT) : ModdedShaderPipeline.getShader(EIShaders.ARMOR_CUTOUT);
        }
        return original.call();
    }
}
