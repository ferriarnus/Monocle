package dev.ferriarnus.monocle.moddedshaders.mixin.shaderchanges;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.Monocle;
import net.irisshaders.iris.Iris;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

@Mixin(targets = "mekanism/client/render/entity/RenderFlame")
public class MixinRenderFlame {
    private static Object MEKANISM_FLAME;

    static {
        try {
            MEKANISM_FLAME = Class.forName("mekanism.client.render.MekanismRenderType").getField("FLAME").get(null);
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            Monocle.LOGGER.error("Failed to get Mekanism flame!");
        }
    }

    @WrapOperation(method = {
            "render(Lmekanism/common/entity/EntityFlame;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
    }, at = @At(value = "FIELD", target = "Lmekanism/client/render/MekanismRenderType;FLAME:Ljava/util/function/Function;"))
    private Function<ResourceLocation, RenderType> switchShaders(Operation<Function<ResourceLocation, RenderType>> original) {
        if (Iris.isPackInUseQuick() && Iris.getIrisConfig().shouldAllowUnknownShaders()) {
            return (Function<ResourceLocation, RenderType>) MEKANISM_FLAME;
        }
        return original.call();
    }
}
