package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.TWFShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Config("twilightforest")
@Mixin(targets = "twilightforest/client/renderer/block/RedThreadRenderer", remap = false)
public class MixinRedThreadRenderer {

    @WrapOperation(method = "render(Ltwilightforest/block/entity/RedThreadBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At(value = "FIELD", target = "Ltwilightforest/client/renderer/block/RedThreadRenderer;GLOW:Lnet/minecraft/client/renderer/RenderType;"))
    public RenderType wrapThread(Operation<RenderType> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return TWFShaders.GLOW;
        }
        return original.call();
    }
}
