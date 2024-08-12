package dev.ferriarnus.monocle.embeddiumCompatibility.impl.shader_overrides;


import org.embeddedt.embeddium.impl.gl.shader.GlProgram;

public interface ShaderChunkRendererExt {
	GlProgram<IrisChunkShaderInterface> iris$getOverride();
}
