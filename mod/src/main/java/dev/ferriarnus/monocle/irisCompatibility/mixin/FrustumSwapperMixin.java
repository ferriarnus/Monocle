package dev.ferriarnus.monocle.irisCompatibility.mixin;

import net.irisshaders.iris.shadows.frustum.CullEverythingFrustum;
import net.irisshaders.iris.shadows.frustum.advanced.AdvancedShadowCullingFrustum;
import net.irisshaders.iris.shadows.frustum.advanced.ReversedAdvancedShadowCullingFrustum;
import net.irisshaders.iris.shadows.frustum.fallback.BoxCullingFrustum;
import net.irisshaders.iris.shadows.frustum.fallback.NonCullingFrustum;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.embeddedt.embeddium.impl.render.viewport.frustum.Frustum;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Attach the Embeddium frustum interface to the Iris frustums. The Sodium interface will be stripped by our mixin
 * plugin.
 */
@Mixin({AdvancedShadowCullingFrustum.class, ReversedAdvancedShadowCullingFrustum.class, CullEverythingFrustum.class, BoxCullingFrustum.class, NonCullingFrustum.class})
public abstract class FrustumSwapperMixin implements Frustum, ViewportProvider {
}
