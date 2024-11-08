package dev.ferriarnus.monocle.moddedshaders.mixin;

import dev.ferriarnus.monocle.moddedshaders.MekShader;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.pathways.LightningHandler;
import net.irisshaders.iris.pipeline.ShaderRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Function;

@Mixin(targets = "mekanism/client/render/lib/effect/BillboardingEffectRenderer", remap = false)
public class BillboardingEffectRendererMixin {

    private static Object SPS;

    static {
        try {
            SPS = Class.forName("mekanism.client.render.MekanismRenderType").getField("SPS").get(null);
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            Iris.logger.fatal("Failed to get Mekanism SPS!");
        }
    }

    @Redirect(method = "render(Lnet/minecraft/resources/ResourceLocation;Ljava/lang/String;Ljava/util/function/Supplier;)V", at = @At(value = "FIELD", target = "Lmekanism/client/render/MekanismRenderType;SPS:Ljava/util/function/Function;"))
    private static Function<ResourceLocation, RenderType> doNotSwitchShaders() {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return MekShader.SPS_RENDERTYPE;
        } else {
            return (Function<ResourceLocation, RenderType>) SPS;
        }
    }
}
