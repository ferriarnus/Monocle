package dev.ferriarnus.monocle.moddedshaders.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ferriarnus.monocle.Monocle;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.culling.Frustum;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LevelRenderer.class, priority = 1001)
public class MixinLevelRenderer {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FogRenderer;setupNoFog()V", shift = At.Shift.AFTER))
    public void renderLast(DeltaTracker p_348530_, boolean p_109603_, Camera p_109604_, GameRenderer p_109605_, LightTexture p_109606_, Matrix4f p_254120_, Matrix4f p_323920_, CallbackInfo ci) {
        if (Monocle.moveRenderLastStage()) {
            net.neoforged.neoforge.client.ClientHooks.dispatchRenderStage(net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_LEVEL, this.minecraft.levelRenderer, null, p_254120_, p_323920_, this.minecraft.levelRenderer.getTicks(), p_109604_, this.minecraft.levelRenderer.getFrustum());
            if (Monocle.endBatch() && ImmediateState.isRenderingLevel) {
                Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
            }
        }
    }

    @Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endLastBatch()V", ordinal = 2))
    public void endBatch(DeltaTracker p_348530_, boolean p_109603_, Camera p_109604_, GameRenderer p_109605_, LightTexture p_109606_, Matrix4f p_254120_, Matrix4f p_323920_, CallbackInfo ci) {
        if (Monocle.endBatch() && ImmediateState.isRenderingLevel) {
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        }
    }
}
