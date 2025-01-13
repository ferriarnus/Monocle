package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import dev.ferriarnus.monocle.moddedshaders.mods.FurnitureShader;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Config("refurbished_furniture")
@Mixin(targets = "com/mrcrayfish/furniture/refurbished/client/DeferredElectricRenderer")
public class MixinDeferredElectricRenderer {

    @WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;entityTarget()Lcom/mojang/blaze3d/pipeline/RenderTarget;"))
    public RenderTarget wrapTarget(LevelRenderer instance, Operation<RenderTarget> original) {
        return FurnitureShader.getLightning();
    }
}
