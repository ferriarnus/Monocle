package dev.ferriarnus.monocle.embeddiumCompatibility.mixin.options;

import net.irisshaders.iris.Iris;
import org.embeddedt.embeddium.impl.gui.EmbeddiumOptions;
import org.embeddedt.embeddium.impl.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Disables fog occlusion when a shader pack is enabled, since shaders are not guaranteed to actually implement fog.
 */
@Mixin(RenderSectionManager.class)
public class MixinRenderSectionManager {
	@Redirect(method = "getSearchDistance", remap = false,
		at = @At(value = "FIELD",
			target = "Lorg/embeddedt/embeddium/impl/gui/EmbeddiumOptions$PerformanceSettings;useFogOcclusion:Z",
			remap = false))
	private boolean iris$disableFogOcclusion(EmbeddiumOptions.PerformanceSettings settings) {
		if (Iris.getCurrentPack().isPresent()) {
			return false;
		} else {
			return settings.useFogOcclusion;
		}
	}
}
