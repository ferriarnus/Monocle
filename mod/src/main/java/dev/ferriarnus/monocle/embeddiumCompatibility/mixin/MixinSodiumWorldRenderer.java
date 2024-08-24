package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.server.level.BlockDestructionProgress;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.SortedSet;

@Mixin(EmbeddiumWorldRenderer.class)
public class MixinSodiumWorldRenderer {
	@Redirect(method = "setupTerrain", remap = false,
		at = @At(value = "INVOKE",
			target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSectionManager;needsUpdate()Z",
			remap = false))
	private boolean iris$forceChunkGraphRebuildInShadowPass(RenderSectionManager instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			// TODO: Detect when the sun/moon isn't moving
			return true;
		} else {
			return instance.needsUpdate();
		}
	}

	@Unique
	private static boolean renderLightsOnly = false;
	@Unique
	private static int beList = 0;

	static {
		ShadowRenderingState.setBlockEntityRenderFunction((shadowRenderer, bufferSource, modelView, camera, cameraX, cameraY, cameraZ, tickDelta, hasEntityFrustum, lightsOnly) -> {
			renderLightsOnly = lightsOnly;

			EmbeddiumWorldRenderer.instance().renderBlockEntities(modelView.last().pose(), bufferSource, ((LevelRendererAccessor) Minecraft.getInstance().levelRenderer).getDestructionProgress(), camera, tickDelta);

			int finalBeList = beList;

			beList = 0;

			return finalBeList;
		});
	}

	@Shadow
	private static void renderBlockEntity(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity) {
		throw new IllegalStateException("maybe get Mixin?");
	}

	@Inject(method = "renderBlockEntities(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;)V", at = @At("HEAD"))
	private void resetEntityList(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher blockEntityRenderer, CallbackInfo ci) {
		beList = 0;
	}

	@Inject(method = "renderBlockEntity", at = @At("HEAD"), cancellable = true)
	private static void checkRenderShadow(PoseStack matrices, RenderBuffers bufferBuilders, Long2ObjectMap<SortedSet<BlockDestructionProgress>> blockBreakingProgressions, float tickDelta, MultiBufferSource.BufferSource immediate, double x, double y, double z, BlockEntityRenderDispatcher dispatcher, BlockEntity entity, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			if (renderLightsOnly && entity.getBlockState().getLightEmission() == 0) {
				ci.cancel();
			}
			beList++;
		}
	}

	@Redirect(method = "renderGlobalBlockEntities", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/EmbeddiumWorldRenderer;renderBlockEntity(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/RenderBuffers;Lit/unimi/dsi/fastutil/longs/Long2ObjectMap;FLnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDLnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;Lnet/minecraft/world/level/block/entity/BlockEntity;)V"))
	private void addToList2(PoseStack bufferBuilder, RenderBuffers entry, Long2ObjectMap<SortedSet<BlockDestructionProgress>> transformer, float stage, MultiBufferSource.BufferSource matrices, double bufferBuilders, double blockBreakingProgressions, double tickDelta, BlockEntityRenderDispatcher immediate, BlockEntity x) {
		if (!renderLightsOnly || x.getBlockState().getLightEmission() > 0) {
			renderBlockEntity(bufferBuilder, entry, transformer, stage, matrices, bufferBuilders, blockBreakingProgressions, tickDelta, immediate, x);
			beList++;
		}
	}

	@Inject(method = "isEntityVisible", at = @At("HEAD"), cancellable = true)
	private void iris$overrideEntityCulling(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) cir.setReturnValue(true);
	}
}
