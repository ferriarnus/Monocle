package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.ferriarnus.monocle.moddedshaders.config.Config;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Config("eternal_starlight")
@Mixin(targets = "cn/leolezury/eternalstarlight/common/client/renderer/blockentity/ESPortalRenderer")
public class MixinESPortalRenderer {

    @Inject(method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V", at = @At("HEAD"), cancellable = true)
    public void noShadow(BlockEntity portal, float f, PoseStack stack, MultiBufferSource bufferSource, int light, int overlay, CallbackInfo ci) {
        if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            ci.cancel();
        }
    }
}
