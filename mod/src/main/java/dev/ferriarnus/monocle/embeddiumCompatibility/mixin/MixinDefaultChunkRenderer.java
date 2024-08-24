package dev.ferriarnus.monocle.embeddiumCompatibility.mixin;

import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain.IrisChunkMeshAttributes;
import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain.IrisModelVertexFormats;
import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain.XHFPModelVertexType;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexAttributeBinding;
import org.embeddedt.embeddium.impl.gl.attribute.GlVertexFormat;
import org.embeddedt.embeddium.impl.gl.device.RenderDevice;
import org.embeddedt.embeddium.impl.gui.EmbeddiumOptions;
import org.embeddedt.embeddium.impl.render.chunk.DefaultChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.ShaderChunkRenderer;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderBindingPoints;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkMeshAttribute;
import org.embeddedt.embeddium.impl.render.chunk.vertex.format.ChunkVertexType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultChunkRenderer.class)
public abstract class MixinDefaultChunkRenderer extends ShaderChunkRenderer {
	@Shadow
	private boolean isIndexedPass;

	public MixinDefaultChunkRenderer(RenderDevice device, ChunkVertexType vertexType) {
		super(device, vertexType);
	}


	@Redirect(method = "render", at = @At(value = "FIELD", target = "Lorg/embeddedt/embeddium/impl/gui/EmbeddiumOptions$PerformanceSettings;useBlockFaceCulling:Z"), remap = false)
	private boolean iris$disableBlockFaceCullingInShadowPass(EmbeddiumOptions.PerformanceSettings instance) {
		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered()) return false;
		return instance.useBlockFaceCulling;
	}

//	// TODO
//	@WrapOperation(method = "prepareTessellation", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/chunk/DefaultChunkRenderer;createRegionTessellation(Lorg/embeddedt/embeddium/impl/gl/device/CommandList;Lorg/embeddedt/embeddium/impl/render/chunk/region/RenderRegion$DeviceResources;)Lorg/embeddedt/embeddium/impl/gl/tessellation/GlTessellation;"), remap = false)
//	private GlTessellation doNotSortInShadow(DefaultChunkRenderer instance, CommandList commandList, RenderRegion.DeviceResources resources, Operation<GlTessellation> original) {
//		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() && this.isIndexedPass) return null;
//
//		return original.call(instance, commandList, resources);
//	}
//
//	// TODO
//	@WrapOperation(method = "prepareTessellation", at = @At(value = "INVOKE", target = "Lorg/embeddedt/embeddium/impl/render/chunk/region/RenderRegion$DeviceResources;updateIndexedTessellation(Lorg/embeddedt/embeddium/impl/gl/device/CommandList;Lorg/embeddedt/embeddium/impl/gl/tessellation/GlTessellation;)V"), remap = false)
//	private void doNotSortInShadow2(RenderRegion.DeviceResources instance, CommandList commandList, GlTessellation tessellation, Operation<Void> original) {
//		if (ShadowRenderingState.areShadowsCurrentlyBeingRendered() && this.isIndexedPass) return;
//
//		original.call(instance, commandList, tessellation);
//	}

	@Inject(method = "getBindingsForType", at = @At("TAIL"), cancellable = true)
	private void addType(CallbackInfoReturnable<GlVertexAttributeBinding[]> cir) {
		if (this.vertexType == IrisModelVertexFormats.MODEL_VERTEX_XHFP) {
			GlVertexFormat<ChunkMeshAttribute> vertexFormat = XHFPModelVertexType.VERTEX_FORMAT;
			GlVertexAttributeBinding[] attributes = new GlVertexAttributeBinding[]{
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
			cir.setReturnValue(attributes);
		}
	}
}
