package dev.ferriarnus.monocle.irisCompatibility.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ferriarnus.monocle.irisCompatibility.impl.BoxCullingFrustum;
import dev.ferriarnus.monocle.irisCompatibility.impl.CullEverythingFrustum;
import dev.ferriarnus.monocle.irisCompatibility.impl.NonCullingFrustum;
import dev.ferriarnus.monocle.irisCompatibility.impl.ReversedAdvancedShadowCullingFrustum;
import net.irisshaders.iris.compat.dh.DHCompat;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.irisshaders.iris.shadows.frustum.BoxCuller;
import net.irisshaders.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.renderer.culling.Frustum;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ShadowRenderer.class)
public class ShadowRendererMixin {

    @Final
    @Shadow
    private boolean shouldRenderDH;
    @Final
    @Shadow
    private float halfPlaneLength;

    @ModifyArg(method = "createShadowFrustum", at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;setInfo(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/lang/String;Ljava/lang/String;)Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;"))
    private Frustum changeEverythingCuller(Frustum frustum) {
        return new CullEverythingFrustum();
    }

    @ModifyArg(method = "createShadowFrustum", at = @At(ordinal = 1, value = "INVOKE", target = "Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;setInfo(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/lang/String;Ljava/lang/String;)Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;"))
    private Frustum changeReverseCuller(Frustum frustum, @Local(name = "shadowLightVectorFromOrigin") Vector3f shadowLightVectorFromOrigin, @Local BoxCuller boxCuller, @Local(argsOnly = true) float renderMultiplier) {
        return new ReversedAdvancedShadowCullingFrustum(CapturedRenderingState.INSTANCE.getGbufferModelView(), this.shouldRenderDH && DHCompat.hasRenderingEnabled() ? DHCompat.getProjection() : CapturedRenderingState.INSTANCE.getGbufferProjection(), shadowLightVectorFromOrigin, boxCuller, new BoxCuller(this.halfPlaneLength * renderMultiplier));
    }

    @ModifyArg(method = "createShadowFrustum", at = @At(ordinal = 2, value = "INVOKE", target = "Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;setInfo(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/lang/String;Ljava/lang/String;)Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;"))
    private Frustum changeAdvShadowCuller(Frustum frustum, @Local(name = "shadowLightVectorFromOrigin") Vector3f shadowLightVectorFromOrigin, @Local BoxCuller boxCuller) {
        return new AdvancedShadowCullingFrustum(CapturedRenderingState.INSTANCE.getGbufferModelView(), this.shouldRenderDH && DHCompat.hasRenderingEnabled() ? DHCompat.getProjection() : CapturedRenderingState.INSTANCE.getGbufferProjection(), shadowLightVectorFromOrigin, boxCuller);
    }

    @ModifyArg(method = "createShadowFrustum", at = @At(ordinal = 3, value = "INVOKE", target = "Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;setInfo(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/lang/String;Ljava/lang/String;)Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;"))
    private Frustum changeBoxCuller(Frustum frustum, @Local BoxCuller boxCuller) {
        return new BoxCullingFrustum(boxCuller);
    }

    @ModifyArg(method = "createShadowFrustum", at = @At(ordinal = 4, value = "INVOKE", target = "Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;setInfo(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/lang/String;Ljava/lang/String;)Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;"))
    private Frustum changeNonCuller(Frustum frustum) {
        return new NonCullingFrustum();
    }
}
