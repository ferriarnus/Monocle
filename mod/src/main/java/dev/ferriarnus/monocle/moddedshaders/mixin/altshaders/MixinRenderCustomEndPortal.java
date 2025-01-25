package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import codechicken.lib.render.shader.CCUniform;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.EnderStorageShaders;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;

@Config("enderstorage")
@Mixin(targets = "codechicken/enderstorage/client/render/RenderCustomEndPortal")
public abstract class MixinRenderCustomEndPortal {

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderStateShard$ShaderStateShard;<init>(Ljava/util/function/Supplier;)V"))
    private static Supplier<ShaderInstance> wrapShader(Supplier<ShaderInstance> shader) {
        return () -> {
            if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
                return ModdedShaderPipeline.getShader(EnderStorageShaders.STARFIELD);
            }
            return shader.get();
        };
    }

    @WrapOperation(method = "render(Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At(value = "INVOKE", target = "Lcodechicken/lib/render/shader/CCUniform;glUniform1f(F)V", ordinal = 0))
    public void wrapTime(CCUniform instance, float v, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            ModdedShaderPipeline.getShader(EnderStorageShaders.STARFIELD).getUniform("Time").set(v);
        } else {
            original.call(instance, v);
        }
    }

    @WrapOperation(method = "render(Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At(value = "INVOKE", target = "Lcodechicken/lib/render/shader/CCUniform;glUniform1f(F)V", ordinal = 1))
    public void wrapYaw(CCUniform instance, float v, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            ModdedShaderPipeline.getShader(EnderStorageShaders.STARFIELD).getUniform("Yaw").set(v);
        } else {
            original.call(instance, v);
        }
    }

    @WrapOperation(method = "render(Lcodechicken/lib/vec/Matrix4;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At(value = "INVOKE", target = "Lcodechicken/lib/render/shader/CCUniform;glUniform1f(F)V", ordinal = 2))
    public void wrapPitch(CCUniform instance, float v, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            ModdedShaderPipeline.getShader(EnderStorageShaders.STARFIELD).getUniform("Pitch").set(v);
        } else {
            original.call(instance, v);
        }
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At(value = "INVOKE", target = "Lcodechicken/lib/render/shader/CCUniform;glUniform1f(F)V", ordinal = 0))
    public void wrapTime2(CCUniform instance, float v, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            ModdedShaderPipeline.getShader(EnderStorageShaders.STARFIELD).getUniform("Time").set(v);
        } else {
            original.call(instance, v);
        }
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At(value = "INVOKE", target = "Lcodechicken/lib/render/shader/CCUniform;glUniform1f(F)V", ordinal = 1))
    public void wrapYaw2(CCUniform instance, float v, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            ModdedShaderPipeline.getShader(EnderStorageShaders.STARFIELD).getUniform("Yaw").set(v);
        } else {
            original.call(instance, v);
        }
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V", at = @At(value = "INVOKE", target = "Lcodechicken/lib/render/shader/CCUniform;glUniform1f(F)V", ordinal = 2))
    public void wrapPitch2(CCUniform instance, float v, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            ModdedShaderPipeline.getShader(EnderStorageShaders.STARFIELD).getUniform("Pitch").set(v);
        } else {
            original.call(instance, v);
        }
    }
}
