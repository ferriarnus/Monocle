package dev.ferriarnus.monocle.irisCompatibility.impl;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import dev.ferriarnus.monocle.Monocle;
import dev.ferriarnus.monocle.ShaderTransformer;
import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain.IrisModelVertexFormats;
import dev.ferriarnus.monocle.embeddiumCompatibility.impl.vertices.terrain.XHFPTerrainVertex;
import net.caffeinemc.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.shaderpack.programs.ProgramFallbackResolver;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.irisshaders.iris.targets.RenderTargets;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.irisshaders.iris.vertices.sodium.terrain.FormatAnalyzer;
import net.minecraft.resources.ResourceLocation;
import org.embeddedt.embeddium.impl.gl.GlObject;
import org.embeddedt.embeddium.impl.gl.shader.GlProgram;
import org.embeddedt.embeddium.impl.gl.shader.GlShader;
import org.embeddedt.embeddium.impl.gl.shader.ShaderType;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderBindingPoints;
import org.embeddedt.embeddium.impl.render.chunk.shader.ChunkShaderInterface;
import org.embeddedt.embeddium.impl.render.chunk.terrain.DefaultTerrainRenderPasses;
import org.embeddedt.embeddium.impl.render.chunk.terrain.TerrainRenderPass;
import org.lwjgl.opengl.GL43C;
import org.taumc.glsl.Main;

import java.util.*;
import java.util.function.Supplier;

public class EmbeddiumPrograms {
	private final EnumMap<Pass, GlFramebuffer> framebuffers = new EnumMap<>(Pass.class);
	private final EnumMap<Pass, GlProgram<ChunkShaderInterface>> shaders = new EnumMap<>(Pass.class);

	private boolean hasBlockId;
	private boolean hasMidUv;
	private boolean hasNormal;
	private boolean hasMidBlock;

	public EmbeddiumPrograms(IrisRenderingPipeline pipeline, ProgramSet programSet, ProgramFallbackResolver resolver,
							 RenderTargets renderTargets, Supplier<ShadowRenderTargets> shadowRenderTargets,
							 CustomUniforms customUniforms) {
		Stopwatch stopwatch = Stopwatch.createStarted();
		Monocle.LOGGER.info("Transforming Embeddium shaders...");

		for (Pass pass : Pass.values()) {
			ProgramSource source = resolver.resolveNullable(pass.getOriginalId());
			Supplier<ImmutableSet<Integer>> flipState = getFlipState(pipeline, pass, pass == Pass.SHADOW || pass == Pass.SHADOW_CUTOUT);
			GlFramebuffer framebuffer = createFramebuffer(pass, source, shadowRenderTargets, renderTargets, flipState);
			framebuffers.put(pass, framebuffer);

			if (source == null) {
				continue;
			}

			AlphaTest alphaTest = getAlphaTest(pass, source);
			Map<PatchShaderType, String> transformed = transformShaders(source, alphaTest, programSet);
			GlProgram<ChunkShaderInterface> shader = createShader(pipeline, pass, source, alphaTest, customUniforms, flipState, createGlShaders(pass.name().toLowerCase(Locale.ROOT), transformed));
			shaders.put(pass, shader);
		}

		stopwatch.stop();
		Monocle.LOGGER.info("Transforming Embeddium shaders completed in {}", stopwatch);

		WorldRenderingSettings.INSTANCE.setVertexFormat((ChunkVertexType) IrisModelVertexFormats.MODEL_VERTEX_XHFP);

	}

	private AlphaTest getAlphaTest(Pass pass, ProgramSource source) {
		return source.getDirectives().getAlphaTestOverride().orElse(
			pass == Pass.TERRAIN_CUTOUT || pass == Pass.SHADOW_CUTOUT ? AlphaTests.ONE_TENTH_ALPHA : AlphaTest.ALWAYS);
	}

	private Map<PatchShaderType, String> transformShaders(ProgramSource source, AlphaTest alphaTest, ProgramSet programSet) {
//		Map<PatchShaderType, String> transformed = EmbeddiumTransformPatcher.patchEmbeddium(
//			source.getName(),
//			source.getVertexSource().orElse(null),
//			source.getGeometrySource().orElse(null),
//			source.getTessControlSource().orElse(null),
//			source.getTessEvalSource().orElse(null),
//			source.getFragmentSource().orElse(null),
//			alphaTest, IrisModelVertexFormats.MODEL_VERTEX_XHFP,
//			programSet.getPackDirectives().getTextureMap());

		Map<PatchShaderType, String> transformedNew = ShaderTransformer.transform(
				source.getName(),
				source.getVertexSource().orElse(null),
				source.getGeometrySource().orElse(null),
				source.getTessControlSource().orElse(null),
				source.getTessEvalSource().orElse(null),
				source.getFragmentSource().orElse(null),
				alphaTest, IrisModelVertexFormats.MODEL_VERTEX_XHFP,
				programSet.getPackDirectives().getTextureMap());

		//ShaderPrinter.printProgram("old_" + source.getName()).addSources(transformed).print();
		ShaderPrinter.printProgram("new_" + source.getName()).addSources(transformedNew).print();

		return transformedNew;
	}

	private Map<PatchShaderType, GlShader> createGlShaders(String passName, Map<PatchShaderType, String> transformed) {
		Map<PatchShaderType, GlShader> newMap = new EnumMap<>(PatchShaderType.class);
		for (Map.Entry<PatchShaderType, String> entry : transformed.entrySet()) {
			if (entry.getValue() == null) continue;
			newMap.put(entry.getKey(), new GlShader(fromGlShaderType(entry.getKey().glShaderType),
				ResourceLocation.fromNamespaceAndPath("iris", "embeddium-shader-" + passName), entry.getValue()));
		}
		return newMap;
	}

	ShaderType fromGlShaderType(net.irisshaders.iris.gl.shader.ShaderType type) {
        return switch (type) {
            case VERTEX -> ShaderType.VERTEX;
            case GEOMETRY -> ShaderType.GEOM;
            case FRAGMENT -> ShaderType.FRAGMENT;
            case TESSELATION_CONTROL -> ShaderType.TESS_CTRL;
            default -> ShaderType.TESS_EVALUATE;
        };
	}

	private Supplier<ImmutableSet<Integer>> getFlipState(IrisRenderingPipeline pipeline, Pass pass, boolean isShadowPass) {
		if (isShadowPass) {
			return pipeline::getFlippedBeforeShadow;
		}
		return () -> pass == Pass.TRANSLUCENT ? pipeline.getFlippedAfterTranslucent() : pipeline.getFlippedAfterPrepare();
	}

	private GlProgram<ChunkShaderInterface> createShader(IrisRenderingPipeline pipeline, Pass pass, ProgramSource source,
														 AlphaTest alphaTest,
														 CustomUniforms customUniforms, Supplier<ImmutableSet<Integer>> flipState,
														 Map<PatchShaderType, GlShader> transformed) {
		GlProgram.Builder builder = GlProgram.builder(ResourceLocation.fromNamespaceAndPath("embeddium", "chunk_shader_for_" + pass.name().toLowerCase(Locale.ROOT)));

		for (GlShader shader : transformed.values()) {
			builder.attachShader(shader);
		}

		boolean containsTessellation = source.getTessEvalSource().isPresent();

		try {
			return buildProgram(builder, pipeline, pass, source, alphaTest, customUniforms, flipState, containsTessellation);
		} finally {
			transformed.values().forEach(GlShader::delete);
		}
	}

	private GlFramebuffer createFramebuffer(Pass pass, ProgramSource source,
											Supplier<ShadowRenderTargets> shadowRenderTargets,
											RenderTargets renderTargets,
											Supplier<ImmutableSet<Integer>> flipState) {
		if (pass == Pass.SHADOW || pass == Pass.SHADOW_CUTOUT || pass == Pass.SHADOW_TRANS) {
			return shadowRenderTargets.get().createShadowFramebuffer(ImmutableSet.of(),
				source == null ? new int[]{0, 1} : (source.getDirectives().hasUnknownDrawBuffers() ? new int[]{0, 1} : source.getDirectives().getDrawBuffers()));
		} else {
			return renderTargets.createGbufferFramebuffer(flipState.get(), source == null ? new int[]{0, 1} : (source.getDirectives().hasUnknownDrawBuffers() ? new int[]{0} : source.getDirectives().getDrawBuffers()));
		}
	}

	private List<BufferBlendOverride> createBufferBlendOverrides(ProgramSource source) {
		List<BufferBlendOverride> overrides = new ArrayList<>();
		source.getDirectives().getBufferBlendOverrides().forEach(information -> {
			int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.index());
			if (index > -1) {
				overrides.add(new BufferBlendOverride(index, information.blendMode()));
			}
		});
		return overrides;
	}

	private GlProgram<ChunkShaderInterface> buildProgram(GlProgram.Builder builder, IrisRenderingPipeline pipeline,
														 Pass pass, ProgramSource source, AlphaTest alphaTest, CustomUniforms customUniforms,
														 Supplier<ImmutableSet<Integer>> flipState,
														 boolean containsTessellation) {
		return builder
			.bindAttribute("a_PosId", ChunkShaderBindingPoints.ATTRIBUTE_POSITION_ID)
			.bindAttribute("a_Color", ChunkShaderBindingPoints.ATTRIBUTE_COLOR)
			.bindAttribute("a_TexCoord", ChunkShaderBindingPoints.ATTRIBUTE_BLOCK_TEXTURE)
			.bindAttribute("a_LightAndData", ChunkShaderBindingPoints.ATTRIBUTE_LIGHT_TEXTURE)
			.bindAttribute("mc_Entity", 11)
			.bindAttribute("mc_midTexCoord", 12)
			.bindAttribute("at_tangent", 13)
			.bindAttribute("iris_Normal", 10)
			.bindAttribute("at_midBlock", 14)
			.link((shader) -> {
				int handle = ((GlObject) shader).handle();
				GLDebug.nameObject(GL43C.GL_PROGRAM, handle, "sodium-terrain-" + pass.toString().toLowerCase(Locale.ROOT));
				if (!hasNormal) hasNormal = GL43C.glGetAttribLocation(handle, "iris_Normal") != -1;
				if (!hasMidBlock) hasMidBlock = GL43C.glGetAttribLocation(handle, "at_midBlock") != -1;
				if (!hasBlockId) hasBlockId = GL43C.glGetAttribLocation(handle, "mc_Entity") != -1;
				if (!hasMidUv) hasMidUv = GL43C.glGetAttribLocation(handle, "mc_midTexCoord") != -1;
				return new EmbeddiumShader(pipeline, pass, shader, handle, source.getDirectives().getBlendModeOverride().orElse(null),
					createBufferBlendOverrides(source), customUniforms, flipState,
					alphaTest.reference(), containsTessellation);
			});
	}

	public GlProgram<ChunkShaderInterface> getProgram(TerrainRenderPass pass) {
		Pass pass2 = mapTerrainRenderPass(pass);
		return this.shaders.get(pass2);
	}

	public GlFramebuffer getFramebuffer(TerrainRenderPass pass) {
		Pass pass2 = mapTerrainRenderPass(pass);
		return this.framebuffers.get(pass2);
	}

	private Pass mapTerrainRenderPass(TerrainRenderPass pass) {
		if (pass == DefaultTerrainRenderPasses.SOLID) {
			return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? Pass.SHADOW : Pass.TERRAIN;
		} else if (pass == DefaultTerrainRenderPasses.CUTOUT) {
			return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? Pass.SHADOW_CUTOUT : Pass.TERRAIN_CUTOUT;
		} else if (pass == DefaultTerrainRenderPasses.TRANSLUCENT) {
			return ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? Pass.SHADOW : Pass.TRANSLUCENT;
		} else {
			throw new IllegalArgumentException("Unknown pass: " + pass);
		}
	}

	public enum Pass {
		SHADOW(ProgramId.ShadowSolid),
		SHADOW_CUTOUT(ProgramId.ShadowCutout),
		SHADOW_TRANS(ProgramId.ShadowWater),
		TERRAIN(ProgramId.TerrainSolid),
		TERRAIN_CUTOUT(ProgramId.TerrainCutout),
		TRANSLUCENT(ProgramId.Water);

		private final ProgramId originalId;

		Pass(ProgramId originalId) {
			this.originalId = originalId;
		}

		public ProgramId getOriginalId() {
			return originalId;
		}
	}
}
