package dev.ferriarnus.monocle.moddedshaders.mixin.shaderchanges;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.Monocle;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "mekanism/client/render/armor/MekaSuitArmor")
public class MixinRenderMekasuit {
    private static Object MEKASUIT;

    static {
        try {
            MEKASUIT = Class.forName("mekanism.client.render.MekanismRenderType").getField("MEKASUIT").get(null);
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            Monocle.LOGGER.error("Failed to get Mekanism flame!");
        }
    }

    @WrapOperation(method = {
            "renderArm",
            "Lmekanism/client/render/armor/MekaSuitArmor;render(Lnet/minecraft/client/model/HumanoidModel;Lnet/minecraft/client/renderer/MultiBufferSource;Lcom/mojang/blaze3d/vertex/PoseStack;IILmekanism/common/lib/Color;ZLnet/minecraft/world/entity/LivingEntity;Ljava/util/Map;Z)V"
    }, at = @At(value = "FIELD", target = "Lmekanism/client/render/MekanismRenderType;MEKASUIT:Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType switchShaders(Operation<RenderType> original) {
        if (Iris.isPackInUseQuick() && Iris.getIrisConfig().shouldAllowUnknownShaders() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return  (RenderType) MEKASUIT;
        }
        return original.call();
    }
}
