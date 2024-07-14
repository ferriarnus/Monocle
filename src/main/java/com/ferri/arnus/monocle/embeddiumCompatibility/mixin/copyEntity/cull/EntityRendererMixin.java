package com.ferri.arnus.monocle.embeddiumCompatibility.mixin.copyEntity.cull;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.embeddedt.embeddium.impl.render.EmbeddiumWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
	@Redirect(
		method = "shouldRender",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/culling/Frustum;isVisible(Lnet/minecraft/world/phys/AABB;)Z",
			ordinal = 0
		)
	)
	private boolean preShouldRender(Frustum instance, AABB box, Entity entity) {
		EmbeddiumWorldRenderer renderer = EmbeddiumWorldRenderer.instanceNullable();
		if (renderer == null) {
			return instance.isVisible(box);
		} else {
			return instance.isVisible(box) && renderer.isEntityVisible(entity);
		}
	}
}
