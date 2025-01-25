package dev.ferriarnus.monocle.moddedshaders.mixin.altshaders;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import dev.ferriarnus.monocle.moddedshaders.mods.SVShaders;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.ShaderInstance;
import net.povstalec.stellarview.client.render.shader.StarShaderInstance;
import net.povstalec.stellarview.client.render.shader.StellarViewShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Config("stellarview")
@Mixin(targets = "net/povstalec/stellarview/client/util/StarBuffer")
public class MixinStarBuffer {

    @WrapOperation(method = "_drawWithShader", at = @At(value = "INVOKE", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;setSampler(Ljava/lang/String;Ljava/lang/Object;)V"))
    public void changeSampler(StarShaderInstance instance, String s, Object o, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            //monocle$getShaderInstance(instance).setSampler("Monocle" + s, o);
            return;
        }
        original.call(instance, s, o);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;MODEL_VIEW_MATRIX:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeModelMatrix(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("ModelViewMat");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;PROJECTION_MATRIX:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeProjectionMatrix(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("ProjMat");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;COLOR_MODULATOR:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeColor(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("ColorModulator");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;FOG_START:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeFogStart(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("FogStart");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;FOG_END:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeFogEnd(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("FogEnd");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;FOG_SHAPE:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeFogShape(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("FogShape");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;FOG_COLOR:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeFogcolor(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("FogColor");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;TEXTURE_MATRIX:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeTextureMatrix(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("TextureMat");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;GAME_TIME:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeGameTime(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("GameTime");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;SCREEN_SIZE:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeScreenSize(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("ScreenSize");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;LINE_WIDTH:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeLineWidth(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("LineWidth");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;RELATIVE_SPACE_LY:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeLY(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("RelativeSpaceLy");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;RELATIVE_SPACE_KM:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeKM(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("RelativeSpaceKm");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;LENSING_MAT:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeLensMat(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("LensingMat");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;LENSING_MAT_INV:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeLensMatInv(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("LensingMatInv");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "FIELD", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;LENSING_INTENSITY:Lcom/mojang/blaze3d/shaders/Uniform;"))
    public Uniform changeLensInt(StarShaderInstance instance, Operation<Uniform> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return monocle$getShaderInstance(instance).getUniform("LensingIntensity");
        }
        return original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setupShaderLights(Lnet/minecraft/client/renderer/ShaderInstance;)V"))
    public void changeShaderLights(ShaderInstance instance, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            RenderSystem.setupShaderLights(monocle$getShaderInstance(instance));
            return;
        }
        original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "INVOKE", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;apply()V"))
    public void changeApply(StarShaderInstance instance, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            monocle$getShaderInstance(instance).apply();
            return;
        }
        original.call(instance);
    }

    @WrapOperation(method = "_drawWithShader", at = @At(value = "INVOKE", target = "Lnet/povstalec/stellarview/client/render/shader/StarShaderInstance;apply()V"))
    public void changeClear(StarShaderInstance instance, Operation<Void> original) {
        if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && !ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            monocle$getShaderInstance(instance).clear();
            return;
        }
        original.call(instance);
    }

    @Unique
    private ShaderInstance monocle$getShaderInstance(ShaderInstance shader) {
        String name = shader.getName();
        if (name.equals(StellarViewShaders.starShader().getName())) {
            return ModdedShaderPipeline.getShader(SVShaders.STAR);
        } else if (name.equals(StellarViewShaders.starTexShader().getName())) {
            return ModdedShaderPipeline.getShader(SVShaders.STAR_TEX);
        } else if (name.equals(StellarViewShaders.starDustCloudShader().getName())) {
            return ModdedShaderPipeline.getShader(SVShaders.DUST_CLOUD);
        }
        return shader;
    }
}
