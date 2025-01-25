package dev.ferriarnus.monocle.moddedshaders.mixin.shaderchanges;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.Monocle;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(targets = "mekanism.client.render.lib.effect.BillboardingEffectRenderer")
public class MixinRenderSPS {
    private static Object MEKANISM_SPS;

    static {
        try {
            MEKANISM_SPS = Class.forName("mekanism.client.render.MekanismRenderType").getField("SPS").get(null);
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            Monocle.LOGGER.error("Failed to get SPS!");
        }
    }


    @WrapOperation(method = "render(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;Ljava/util/function/Supplier;)V", at = @At(
            value = "FIELD",
            target = "Lmekanism/client/render/MekanismRenderType;SPS:Ljava/util/function/Function;"))
    private static Function<ResourceLocation, RenderType> switchShaders(Operation<Function<ResourceLocation, RenderType>> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && Iris.getIrisConfig().shouldAllowUnknownShaders()) {
            return (Function<ResourceLocation, RenderType>) MEKANISM_SPS;
        } else {
            return original.call();
        }
    }
}
