package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.TWFShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import twilightforest.client.TFShaders;

@Config("twilightforest")
@Mixin(targets = "twilightforest/client/event/ClientEvents", remap = false)
public class MixinClientEvents {

    @WrapOperation(method = "renderAurora", at = @At(value = "INVOKE", target = "Ltwilightforest/client/TFShaders$PositionAwareShaderInstance;invokeThenEndTesselator(IFFFLcom/mojang/blaze3d/vertex/BufferBuilder;)V"))
    private static void wrapAurora(TFShaders.PositionAwareShaderInstance instance, int seed, float x, float y, float z, BufferBuilder builder, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            var shader = ModdedShaderPipeline.getShader(TWFShaders.AURORA);
            final var last = RenderSystem.getShader();
            RenderSystem.setShader(() -> shader);

            Uniform seedContext = shader.getUniform("SeedContext");
            if (seedContext != null) {
                seedContext.set(seed);
            }

            Uniform positionContext = shader.getUniform("PositionContext");
            if (positionContext != null) {
                positionContext.set(x, y, z);
            }

            shader.apply();

            BufferUploader.drawWithShader(builder.buildOrThrow());

            if (seedContext != null) {
                seedContext.set(0);
            }

            if (positionContext != null) {
                positionContext.set(0f, 0f, 0f);
            }

            shader.clear();
            RenderSystem.setShader(() -> last);
        } else {
            original.call(instance, seed, x, y, z, builder);
        }
    }
}
