package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.Camera;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.embeddedt.embeddium.impl.render.chunk.lists.SortedRenderLists;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManagerShadow {
	@Shadow(remap = false)
	private @NotNull SortedRenderLists renderLists;
	@Unique
	private @NotNull SortedRenderLists shadowRenderLists = SortedRenderLists.empty();

	@Redirect(method = "createTerrainRenderList", at = @At(value = "FIELD", target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSectionManager;renderLists:Lorg/embeddedt/embeddium/impl/render/chunk/lists/SortedRenderLists;"))
	private void useShadowRenderList(RenderSectionManager instance, SortedRenderLists value) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) {
			shadowRenderLists = value;
		} else {
			renderLists = value;
		}
	}

	@Inject(method = "update", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSectionManager;createTerrainRenderList(Lnet/minecraft/client/Camera;Lorg/embeddedt/embeddium/impl/render/viewport/Viewport;IZ)V", shift = At.Shift.AFTER), cancellable = true)
	private void cancelIfShadow(Camera camera, Viewport viewport, int frame, boolean spectator, CallbackInfo ci) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) ci.cancel();
	}

	@Redirect(method = {
		"getRenderLists",
		"getVisibleChunkCount",
		"renderLayer",
		"scheduleTranslucencyUpdates",
		"getSortingStrings"
	}, at = @At(value = "FIELD", target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSectionManager;renderLists:Lorg/embeddedt/embeddium/impl/render/chunk/lists/SortedRenderLists;"), remap = false)
	private SortedRenderLists useShadowRenderList2(RenderSectionManager instance) {
		return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? shadowRenderLists : renderLists;
	}

	@Redirect(method = {
		"resetRenderLists"
	}, at = @At(value = "FIELD", target = "Lorg/embeddedt/embeddium/impl/render/chunk/RenderSectionManager;renderLists:Lorg/embeddedt/embeddium/impl/render/chunk/lists/SortedRenderLists;"), remap = false)
	private void useShadowRenderList3(RenderSectionManager instance, SortedRenderLists value) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) shadowRenderLists = value;
		else renderLists = value;
	}
}
