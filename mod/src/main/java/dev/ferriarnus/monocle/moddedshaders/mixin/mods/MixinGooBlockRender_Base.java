package dev.ferriarnus.monocle.moddedshaders.mixin.mods;

import com.direwolf20.justdirethings.client.blockentityrenders.baseber.GooBlockRender_Base;
import com.direwolf20.justdirethings.client.renderers.OurRenderTypes;
import com.direwolf20.justdirethings.common.blockentities.basebe.GooBlockBE_Base;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.ferriarnus.monocle.moddedshaders.mods.DireShaders;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GooBlockRender_Base.class)
public class MixinGooBlockRender_Base {

//    @WrapOperation(method = "renderTexturePattern", at = @At(value = "FIELD", target = "Lcom/direwolf20/justdirethings/client/renderers/OurRenderTypes;GooPattern:Lnet/minecraft/client/renderer/RenderType;"))
//    public RenderType WrapBuffer(Operation<RenderType> original) {
//        return DireShaders.GooPattern;
//    }
//
//    @WrapOperation(method = "renderTexturePattern", at = @At(value = "FIELD", target = "Lcom/direwolf20/justdirethings/client/renderers/OurRenderTypes;RenderBlockBackface:Lnet/minecraft/client/renderer/RenderType;"))
//    public RenderType WrapBuffer2(Operation<RenderType> original) {
//        return DireShaders.RenderBlockBackface;
//    }

    @Unique
    private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(786432));
    private final MultiBufferSource.BufferSource bufferSource2 = MultiBufferSource.immediate(new ByteBufferBuilder(786432));

    @Inject(method = "renderTexturePattern", at = @At("HEAD"))
    public void head(Direction direction, Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedOverlayIn, float transparency, BlockState pattern, BlockState renderState, GooBlockBE_Base gooBlockBE_base, CallbackInfo ci) {
        if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            if (DireShaders.GOO_TARGET == null) {
                DireShaders.setupRenderTarget();
            }
            DireShaders.GOO_TARGET.bindWrite(true);
        }
    }

    @Inject(method = "renderTexturePattern", at = @At("TAIL"))
    public void tail(Direction direction, Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedOverlayIn, float transparency, BlockState pattern, BlockState renderState, GooBlockBE_Base gooBlockBE_base, CallbackInfo ci) {
        if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
        }
    }

    @WrapOperation(method = "renderTexturePattern",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 0))
    public VertexConsumer WrapBuffer(MultiBufferSource instance, RenderType renderType, Operation<VertexConsumer> original) {
        if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return bufferSource.getBuffer(DireShaders.GooPattern);
        }
        return original.call(instance, renderType);
    }

    @WrapOperation(method = "renderTexturePattern",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", ordinal = 1))
    public VertexConsumer WrapBuffer2(MultiBufferSource instance, RenderType renderType, Operation<VertexConsumer> original) {
        if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            return bufferSource2.getBuffer(DireShaders.RenderBlockBackface);
        }
        return original.call(instance, renderType);
    }

    @Inject(method = "renderTexturePattern",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;", shift = At.Shift.BEFORE, ordinal = 1))
    public void injectEnd1(Direction direction, Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedOverlayIn, float transparency, BlockState pattern, BlockState renderState, GooBlockBE_Base gooBlockBE_base, CallbackInfo ci) {
        if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            bufferSource.endBatch();
        }
    }

    @Inject(method = "renderTexturePattern",
                at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = At.Shift.AFTER))
    public void injectEnd2(Direction direction, Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedOverlayIn, float transparency, BlockState pattern, BlockState renderState, GooBlockBE_Base gooBlockBE_base, CallbackInfo ci) {
        if (!ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
            bufferSource2.endBatch();
        }
    }
}
