package dev.ferriarnus.monocle.moddedshaders.mixin.shaderchanges;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ferriarnus.monocle.Monocle;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.vertices.ImmediateState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "cn/leolezury/eternalstarlight/common/client/renderer/blockentity/ESPortalRenderer")
public class MixinESPortalRenderer {

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At("HEAD"), cancellable = true)
    public void noShadow(BlockEntity par1, float par2, PoseStack par3, MultiBufferSource par4, int par5, int par6, CallbackInfo ci) {
        if (Monocle.noShadowStage()) {
            if (Iris.isPackInUseQuick() && ImmediateState.isRenderingLevel && ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
                ci.cancel();
            }
        }
    }
}
