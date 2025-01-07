package dev.ferriarnus.monocle.moddedshaders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ferriarnus.monocle.Monocle;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer {

    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;dispatchRenderStage(Lnet/neoforged/neoforge/client/event/RenderLevelStageEvent$Stage;Lnet/minecraft/client/renderer/LevelRenderer;Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;ILnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;)V"))
    public void noLast(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick, Camera camera, Frustum frustum, Operation<Void> original) {
        if (!Monocle.moveRenderLastStage()) {
            original.call(stage, levelRenderer, poseStack, modelViewMatrix, projectionMatrix, renderTick, camera, frustum);
        }
    }
}
