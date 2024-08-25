package dev.ferriarnus.monocle.irisCompatibility.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Unique
    private Vector3f shadowLightVectorFromOrigin;
    @Unique
    private BoxCuller box;

    @ModifyArg(method = "createShadowFrustum", at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;setInfo(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/lang/String;Ljava/lang/String;)Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;"))
    private Frustum changeEverythingCuller(Frustum frustum) {
        return new CullEverythingFrustum();
    }

    @WrapOperation(method = "createShadowFrustum", at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;normalize()Lorg/joml/Vector3f;"))
    private Vector3f captureVector(Vector3f instance, Operation<Vector3f> original) {
        this.shadowLightVectorFromOrigin = instance;
        return original.call(instance);
    }

    @WrapOperation(method = "createShadowFrustum", at = @At(value = "NEW", target = "(D)Lnet/irisshaders/iris/shadows/frustum/BoxCuller;"))
    private BoxCuller captureBox(double maxDistance, Operation<BoxCuller> original) {
        this.box = original.call(maxDistance);
        return this.box;
    }

    @ModifyArg(method = "createShadowFrustum", at = @At(ordinal = 1, value = "INVOKE", target = "Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;setInfo(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/lang/String;Ljava/lang/String;)Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;"))
    private Frustum changeReverseCuller(Frustum frustum, @Local(argsOnly = true) float renderMultiplier, @Local BoxCuller boxCuller) {
        return new ReversedAdvancedShadowCullingFrustum(CapturedRenderingState.INSTANCE.getGbufferModelView(), this.shouldRenderDH && DHCompat.hasRenderingEnabled() ? DHCompat.getProjection() : CapturedRenderingState.INSTANCE.getGbufferProjection(), shadowLightVectorFromOrigin, boxCuller, new BoxCuller(this.halfPlaneLength * renderMultiplier));
    }

    @ModifyArg(method = "createShadowFrustum", at = @At(ordinal = 2, value = "INVOKE", target = "Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;setInfo(Lnet/minecraft/client/renderer/culling/Frustum;Ljava/lang/String;Ljava/lang/String;)Lnet/irisshaders/iris/shadows/frustum/FrustumHolder;"))
    private Frustum changeAdvShadowCuller(Frustum frustum) {
        return new AdvancedShadowCullingFrustum(CapturedRenderingState.INSTANCE.getGbufferModelView(), this.shouldRenderDH && DHCompat.hasRenderingEnabled() ? DHCompat.getProjection() : CapturedRenderingState.INSTANCE.getGbufferProjection(), shadowLightVectorFromOrigin, box);
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
