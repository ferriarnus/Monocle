package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.DireShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Config("justdirethings")
@Mixin(targets = "com/direwolf20/justdirethings/client/entityrenders/PortalEntityRender", remap = false)
public class MixinPortalEntityRender {

    @WrapOperation(method = "render(Lcom/direwolf20/justdirethings/common/entities/PortalEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "FIELD", target = "Lcom/direwolf20/justdirethings/client/entityrenders/PortalEntityRender;renderType:Lnet/minecraft/client/renderer/RenderType;"))
    public RenderType wrapRenderType(Operation<RenderType> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return DireShaders.PORTAL_TYPE;
        }
        return original.call();
    }
}
