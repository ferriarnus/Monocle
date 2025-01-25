package dev.ferriarnus.monocle.moddedshaders.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ferriarnus.monocle.Monocle;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientHooks.class)
public class MixinClientHooks {

    @Inject(method = "dispatchRenderStage(Lnet/neoforged/neoforge/client/event/RenderLevelStageEvent$Stage;Lnet/minecraft/client/renderer/LevelRenderer;Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;ILnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;)V",
            at = @At("HEAD"), cancellable = true)
    private static void noShadows(RenderLevelStageEvent.Stage stage, LevelRenderer levelRenderer, PoseStack poseStack, Matrix4f modelViewMatrix, Matrix4f projectionMatrix, int renderTick, Camera camera, Frustum frustum, CallbackInfo ci) {
        if (Monocle.noShadowStage()) {
            if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
                ci.cancel();
            }
        }
    }
}
