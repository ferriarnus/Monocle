package dev.ferriarnus.monocle.embeddiumCompatibility.mixin.shader_overrides;

import net.irisshaders.iris.vertices.ImmediateState;
import org.embeddedt.embeddium.impl.gl.tessellation.GlPrimitiveType;
import org.lwjgl.opengl.GL43C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "org.embeddedt.embeddium.impl.gl.device.GLRenderDevice$ImmediateDrawCommandList", remap = false)
public class MixinGlRenderDevice {
	@Redirect(method = "multiDrawElementsBaseVertex", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/gl/tessellation/GlPrimitiveType;getId()I"))
	private int replaceId(GlPrimitiveType instance) {
		if (ImmediateState.usingTessellation) return GL43C.GL_PATCHES;

		return instance.getId();
	}
}
