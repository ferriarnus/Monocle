package dev.ferriarnus.monocle.irisCompatibility.impl;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.phys.AABB;
import org.embeddedt.embeddium.impl.render.viewport.Viewport;
import org.embeddedt.embeddium.impl.render.viewport.ViewportProvider;
import org.joml.Matrix4f;
import org.joml.Vector3d;

public class NonCullingFrustum extends Frustum implements ViewportProvider, org.embeddedt.embeddium.impl.render.viewport.frustum.Frustum {
    private final Vector3d position = new Vector3d();

    public NonCullingFrustum() {
        super(new Matrix4f(), new Matrix4f());
    }

    // For Immersive Portals
    // NB: The shadow culling in Immersive Portals must be disabled, because when Advanced Shadow Frustum Culling
    //     is not active, we are at a point where we can make no assumptions how the shader pack uses the shadow
    //     pass beyond what it already tells us. So we cannot use any extra fancy culling methods.
    public boolean canDetermineInvisible(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return false;
    }

    public boolean isVisible(AABB box) {
        return true;
    }

    @Override
    public void prepare(double d, double e, double f) {
        this.position.set(d, e, f);
    }

    @Override
    public Viewport sodium$createViewport() {
        return new Viewport(this, position);
    }

    @Override
    public boolean testAab(float v, float v1, float v2, float v3, float v4, float v5) {
        return true;
    }
}
