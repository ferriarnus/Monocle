package dev.ferriarnus.monocle.moddedshaders;

import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Ints;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.odysseus.ithaka.digraph.Digraph;
import dev.ferriarnus.monocle.moddedshaders.impl.IrisRenderingPipelineExtension;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTest;
import net.irisshaders.iris.gl.blending.BlendModeOverride;
import net.irisshaders.iris.gl.blending.BufferBlendOverride;
import net.irisshaders.iris.gl.framebuffer.GlFramebuffer;
import net.irisshaders.iris.gl.state.FogMode;
import net.irisshaders.iris.gl.state.ShaderAttributeInputs;
import net.irisshaders.iris.gl.uniform.DynamicUniformHolder;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.WorldRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ExtendedShader;
import net.irisshaders.iris.pipeline.programs.ShaderCreator;
import net.irisshaders.iris.pipeline.transform.PatchShaderType;
import net.irisshaders.iris.pipeline.transform.ShaderPrinter;
import net.irisshaders.iris.pipeline.transform.TransformPatcher;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.uniforms.CommonUniforms;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import net.irisshaders.iris.uniforms.VanillaUniforms;
import net.irisshaders.iris.uniforms.builtin.BuiltinReplacementUniforms;
import net.irisshaders.iris.uniforms.custom.CustomUniforms;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModdedShaderCreator {


    public static ShaderInstance createShader(String name, ProgramSource source, AlphaTest fallbackAlpha, VertexFormat vertexFormat,
                                        boolean isIntensity, boolean isFullbright, boolean isGlint, boolean isText, IrisRenderingPipeline pipeline) throws IOException {
        var extenedPipeline = (IrisRenderingPipelineExtension) pipeline;
        GlFramebuffer beforeTranslucent = extenedPipeline.getRenderTargets().createGbufferFramebuffer(pipeline.getFlippedAfterPrepare(), source.getDirectives().getDrawBuffers());
        GlFramebuffer afterTranslucent = extenedPipeline.getRenderTargets().createGbufferFramebuffer(pipeline.getFlippedAfterTranslucent(), source.getDirectives().getDrawBuffers());


        ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, isFullbright, false, isGlint, isText, false);

        Supplier<ImmutableSet<Integer>> flipped =
                () -> pipeline.isBeforeTranslucent ? pipeline.getFlippedAfterPrepare() : pipeline.getFlippedAfterTranslucent();

        return ModdedShaderCreator.create(pipeline, name, source, beforeTranslucent, afterTranslucent,
                fallbackAlpha, vertexFormat, inputs, flipped, isIntensity, false, pipeline.getCustomUniforms());
    }

    private static ExtendedShader create(IrisRenderingPipeline parent, String name, ProgramSource source, GlFramebuffer writingToBeforeTranslucent,
                                        GlFramebuffer writingToAfterTranslucent, AlphaTest fallbackAlpha, VertexFormat vertexFormat,
                                        ShaderAttributeInputs inputs, Supplier<ImmutableSet<Integer>> flipped,
                                        boolean isIntensity, boolean isShadowPass, CustomUniforms customUniforms) throws IOException {
        AlphaTest alpha = source.getDirectives().getAlphaTestOverride().orElse(fallbackAlpha);
        BlendModeOverride blendModeOverride = source.getDirectives().getBlendModeOverride().orElse(null);

        Map<PatchShaderType, String> transformedNew = ModdedShaderTransformer.transform(name,
                source.getVertexSource().orElseThrow(RuntimeException::new),
                source.getGeometrySource().orElse(null),
                source.getTessControlSource().orElse(null),
                source.getTessEvalSource().orElse(null),
                source.getFragmentSource().orElseThrow(RuntimeException::new),
                alpha, inputs, parent.getTextureMap());

        String vertex = transformedNew.get(PatchShaderType.VERTEX);
        String geometry = transformedNew.get(PatchShaderType.GEOMETRY);
        String tessControl = transformedNew.get(PatchShaderType.TESS_CONTROL);
        String tessEval = transformedNew.get(PatchShaderType.TESS_EVAL);
        String fragment = transformedNew.get(PatchShaderType.FRAGMENT);

        String shaderJsonString = String.format("""
			    {
			    "blend": {
			        "func": "add",
			        "srcrgb": "srcalpha",
			        "dstrgb": "1-srcalpha"
			    },
			    "vertex": "%s",
			    "fragment": "%s",
			    "attributes": [
			        "Position",
			        "Color",
			        "UV0",
			        "UV1",
			        "UV2",
			        "Normal"
			    ],
			    "uniforms": [
			        { "name": "iris_TextureMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_ModelViewMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_ModelViewMatInverse", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_ProjMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_ProjMatInverse", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
			        { "name": "iris_NormalMat", "type": "matrix3x3", "count": 9, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0 ] },
			        { "name": "iris_ChunkOffset", "type": "float", "count": 3, "values": [ 0.0, 0.0, 0.0 ] },
			        { "name": "iris_ColorModulator", "type": "float", "count": 4, "values": [ 1.0, 1.0, 1.0, 1.0 ] },
			        { "name": "iris_GlintAlpha", "type": "float", "count": 1, "values": [ 1.0 ] },
			        { "name": "iris_FogStart", "type": "float", "count": 1, "values": [ 0.0 ] },
			        { "name": "iris_FogEnd", "type": "float", "count": 1, "values": [ 1.0 ] },
			        { "name": "iris_FogColor", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] },
			        { "name": "iris_OverlayUV", "type": "int", "count": 2, "values": [ 0, 0 ] },
			        { "name": "iris_LightUV", "type": "int", "count": 2, "values": [ 0, 0 ] }
			    ]
			}""", name, name);

        ShaderPrinter.printProgram(name).addSources(transformedNew).addJson(shaderJsonString).print();

        ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(shaderJsonString, vertex, geometry, tessControl, tessEval, fragment);

        List<BufferBlendOverride> overrides = new ArrayList<>();
        source.getDirectives().getBufferBlendOverrides().forEach(information -> {
            int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.index());
            if (index > -1) {
                overrides.add(new BufferBlendOverride(index, information.blendMode()));
            }
        });

        return new ExtendedShader(shaderResourceFactory, name, vertexFormat, tessControl != null || tessEval != null, writingToBeforeTranslucent, writingToAfterTranslucent, blendModeOverride, alpha, uniforms -> {
            CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);
            customUniforms.assignTo(uniforms);
            BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);
            VanillaUniforms.addVanillaUniforms(uniforms);
        }, (samplerHolder, imageHolder) -> parent.addGbufferOrShadowSamplers(samplerHolder, imageHolder, flipped, isShadowPass, inputs.hasTex(), inputs.hasLight(), inputs.hasOverlay()), isIntensity, parent, overrides, customUniforms);
    }

    public static ExtendedShader createShader(String name, ProgramSource source, AlphaTest alpha, VertexFormat vertexFormat, String json, IrisRenderingPipeline pipeline) throws IOException {
        var extenedPipeline = (IrisRenderingPipelineExtension) pipeline;
        GlFramebuffer beforeTranslucent = extenedPipeline.getRenderTargets().createGbufferFramebuffer(pipeline.getFlippedAfterPrepare(), source.getDirectives().getDrawBuffers());
        GlFramebuffer afterTranslucent = extenedPipeline.getRenderTargets().createGbufferFramebuffer(pipeline.getFlippedAfterTranslucent(), source.getDirectives().getDrawBuffers());


        ShaderAttributeInputs inputs = new ShaderAttributeInputs(vertexFormat, false, false, false, false, false);

        Supplier<ImmutableSet<Integer>> flipped =
                () -> pipeline.isBeforeTranslucent ? pipeline.getFlippedAfterPrepare() : pipeline.getFlippedAfterTranslucent();

        Map<PatchShaderType, String> transformedNew = ModdedShaderTransformer.transform(name,
                source.getVertexSource().orElseThrow(RuntimeException::new),
                source.getGeometrySource().orElse(null),
                source.getTessControlSource().orElse(null),
                source.getTessEvalSource().orElse(null),
                source.getFragmentSource().orElseThrow(RuntimeException::new),
                alpha, inputs, pipeline.getTextureMap());

        String vertex = transformedNew.get(PatchShaderType.VERTEX);
        String geometry = transformedNew.get(PatchShaderType.GEOMETRY);
        String tessControl = transformedNew.get(PatchShaderType.TESS_CONTROL);
        String tessEval = transformedNew.get(PatchShaderType.TESS_EVAL);
        String fragment = transformedNew.get(PatchShaderType.FRAGMENT);

        ShaderPrinter.printProgram(name).addSources(transformedNew).addJson(json).print();

        ResourceProvider shaderResourceFactory = new IrisProgramResourceFactory(json, vertex, geometry, tessControl, tessEval, fragment);

        List<BufferBlendOverride> overrides = new ArrayList<>();
        source.getDirectives().getBufferBlendOverrides().forEach(information -> {
            int index = Ints.indexOf(source.getDirectives().getDrawBuffers(), information.index());
            if (index > -1) {
                overrides.add(new BufferBlendOverride(index, information.blendMode()));
            }
        });

        return new ExtendedShader(shaderResourceFactory, name, vertexFormat, tessControl != null || tessEval != null, beforeTranslucent, afterTranslucent, null, alpha, uniforms -> {
            CommonUniforms.addDynamicUniforms(uniforms, FogMode.PER_VERTEX);
            pipeline.getCustomUniforms().assignTo(uniforms);
            BuiltinReplacementUniforms.addBuiltinReplacementUniforms(uniforms);
            VanillaUniforms.addVanillaUniforms(uniforms);
        }, (samplerHolder, imageHolder) -> pipeline.addGbufferOrShadowSamplers(samplerHolder, imageHolder, flipped, false, inputs.hasTex(), inputs.hasLight(), inputs.hasOverlay()), false, pipeline, overrides, pipeline.getCustomUniforms());
    }

    private record IrisProgramResourceFactory(String json, String vertex, String geometry, String tessControl, String tessEval, String fragment) implements ResourceProvider {

        public Optional<Resource> getResource(ResourceLocation id) {
            String path = id.getPath();
            if (path.endsWith("json")) {
                return Optional.of(new ModdedShaderCreator.StringResource(id, this.json));
            } else if (path.endsWith("vsh")) {
                return Optional.of(new ModdedShaderCreator.StringResource(id, this.vertex));
            } else if (path.endsWith("gsh")) {
                return this.geometry == null ? Optional.empty() : Optional.of(new ModdedShaderCreator.StringResource(id, this.geometry));
            } else if (path.endsWith("tcs")) {
                return this.tessControl == null ? Optional.empty() : Optional.of(new ModdedShaderCreator.StringResource(id, this.tessControl));
            } else if (path.endsWith("tes")) {
                return this.tessEval == null ? Optional.empty() : Optional.of(new ModdedShaderCreator.StringResource(id, this.tessEval));
            } else {
                return path.endsWith("fsh") ? Optional.of(new ModdedShaderCreator.StringResource(id, this.fragment)) : Optional.empty();
            }
        }
    }

    private static class StringResource extends Resource {
        private final String content;

        private StringResource(ResourceLocation id, String content) {
            super(new PathPackResources(new PackLocationInfo("<iris shaderpack shaders>", Component.literal("iris"), PackSource.BUILT_IN, Optional.of(new KnownPack("iris", "shader", "1.0"))), IrisPlatformHelpers.getInstance().getConfigDir()), () -> new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
            this.content = content;
        }

        public InputStream open() {
            return IOUtils.toInputStream(this.content, StandardCharsets.UTF_8);
        }
    }
}
