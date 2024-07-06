package com.ferri.arnus.contacts.embeddiumCompatibility.mixin.shadow_map;

import net.irisshaders.iris.shadows.ShadowRenderingState;
import org.embeddedt.embeddium.impl.gui.EmbeddiumOptions;
import org.embeddedt.embeddium.impl.render.chunk.DefaultChunkRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultChunkRenderer.class)
public class MixinDefaultChunkRenderer {
	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lorg/embeddedt/embeddium/impl/gui/EmbeddiumOptions$PerformanceSettings;useBlockFaceCulling:Z"), remap = false)
	private boolean iris$disableBlockFaceCullingInShadowPass(EmbeddiumOptions.PerformanceSettings instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) return false;
		return instance.useBlockFaceCulling;
	}

	//TODO potentially doable, seems the field became an arg
//	@ModifyArg(method = "prepareTessellation", index = 2, at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/chunk/DefaultChunkRenderer;createRegionTessellation(Lorg/embeddedt/embeddium/impl/gl/device/CommandList;Lorg/embeddedt/embeddium/impl/render/chunk/region/RenderRegion$DeviceResources;)Lorg/embeddedt/embeddium/impl/gl/tessellation/GlTessellation;"), remap = false)
//	private boolean doNotSortInShadow(boolean useSharedIndexBuffer) {
//		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) return false;
//
//		return useSharedIndexBuffer;
//	}
}
