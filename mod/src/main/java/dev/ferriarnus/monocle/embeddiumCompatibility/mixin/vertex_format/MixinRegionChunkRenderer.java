package dev.ferriarnus.monocle.embeddiumCompatibility.mixin.vertex_format;

import dev.ferriarnus.monocle.embeddiumCompatibility.impl.IrisChunkShaderBindingPoints;
import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertex_format.IrisChunkMeshAttributes;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttributeBinding;
import org.embeddedt.embeddium.impl.gl.buffer.GlBuffer;
import org.embeddedt.embeddium.impl.gl.device.RenderDevice;
import org.embeddedt.embeddium.impl.gl.tessellation.TessellationBinding;
import org.embeddedt.embeddium.impl.render.chunk.DefaultChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.ShaderChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderBindingPoints;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkMeshAttribute;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinRegionChunkRenderer extends ShaderChunkRenderer {
	public MixinRegionChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
		super(device, vertexType);
	}

	@Redirect(remap = false, method = "createRegionTessellation", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/gl/tessellation/TessellationBinding;forVertexBuffer(Lorg/embeddedt/embeddium/impl/gl/buffer/GlBuffer;[Lorg/embeddedt/embeddium/impl/gl/attribute/GlVertexAttributeBinding;)Lorg/embeddedt/embeddium/impl/gl/tessellation/TessellationBinding;"))
	private TessellationBinding iris$onInit(GlBuffer buffer, GlVertexAttributeBinding[] attributes) {
		if (!WorldRenderingSettings.INSTANCE.shouldUseExtendedVertexFormat()) {
			return TessellationBinding.forVertexBuffer(buffer, attributes);
		}

		attributes = new GlVertexAttributeBinding[]{
			new GlVertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID,
				vertexFormat.getAttribute(ChunkMeshAttribute.POSITION_MATERIAL_MESH)),
			new GlVertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_COLOR,
				vertexFormat.getAttribute(ChunkMeshAttribute.COLOR_SHADE)),
			new GlVertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE,
				vertexFormat.getAttribute(ChunkMeshAttribute.BLOCK_TEXTURE)),
			new GlVertexAttributeBinding(ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE,
				vertexFormat.getAttribute(ChunkMeshAttribute.LIGHT_TEXTURE)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_BLOCK,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_BLOCK)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.BLOCK_ID,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.BLOCK_ID)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.MID_TEX_COORD,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.MID_TEX_COORD)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.TANGENT,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.TANGENT)),
			new GlVertexAttributeBinding(IrisChunkShaderBindingPoints.NORMAL,
				vertexFormat.getAttribute(IrisChunkMeshAttributes.NORMAL))
		};

		return TessellationBinding.forVertexBuffer(buffer, attributes);
	}
}
