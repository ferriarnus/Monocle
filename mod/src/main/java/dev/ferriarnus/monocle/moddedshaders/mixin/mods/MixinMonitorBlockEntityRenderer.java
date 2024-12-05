package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dan200.computercraft.client.render.monitor.MonitorBlockEntityRenderer;
import dev.ferriarnus.monocle.moddedshaders.mods.CCShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MonitorBlockEntityRenderer.class)
public class MixinMonitorBlockEntityRenderer {

    @WrapOperation(method = "renderTerminal", at = @At(value = "FIELD", target = "Ldan200/computercraft/client/render/RenderTypes;MONITOR_TBO:Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType wrapRenderType(Operation<RenderType> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return CCShaders.MONITOR_TBO;
        }
        return original.call();
    }
}