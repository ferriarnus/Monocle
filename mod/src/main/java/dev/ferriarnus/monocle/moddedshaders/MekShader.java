package dev.ferriarnus.monocle.moddedshaders;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.moddedshaders.impl.WorldRenderingPipelineExtension;
import dev.ferriarnus.monocle.moddedshaders.impl.ProgramSetExtension;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.helpers.FakeChainedJsonException;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shaderpack.programs.ProgramSet;
import net.irisshaders.iris.shaderpack.programs.ProgramSource;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;
import java.util.stream.Collectors;

public class MekShader {

    public static final ResourceLocation MEKASUIT = ResourceLocation.fromNamespaceAndPath("mekanism", "mekasuit");
    public static final ResourceLocation SPS = ResourceLocation.fromNamespaceAndPath("mekanism", "sps");

    static {
        init();
    }

    public static final Function<ResourceLocation, RenderType> SPS_RENDERTYPE = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(SPS)))
                .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
                .setTransparencyState(RenderType.LIGHTNING_TRANSPARENCY)
                .setOutputState(RenderType.TRANSLUCENT_TARGET)
                .createCompositeState(false);
        return RenderType.create("mek_sps", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 1_536, false, true, state);
    });

    public static void init() {
        ModdedShaderPipeline.addShader(MEKASUIT, MekShader::createMekasuit);
        ModdedShaderPipeline.addShader(SPS, MekShader::createSPS);
    }

    private static ShaderInstance createMekasuit() {
        try {
            if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline && pipeline instanceof WorldRenderingPipelineExtension extension) {
                return ModdedShaderCreator.createShader("mekasuit", getSource("mekasuit", extension.getProgramSet()), AlphaTests.ONE_TENTH_ALPHA,
                        IrisVertexFormats.ENTITY, false, false, false, false, pipeline);
            }
        } catch (FakeChainedJsonException json) {
            throw json.getTrueException();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mekasuit", e);
        }
        return null;
    }

    private static ShaderInstance createSPS() {
        try {
            if (Iris.getPipelineManager().getPipelineNullable() instanceof IrisRenderingPipeline pipeline && pipeline instanceof WorldRenderingPipelineExtension extension) {
                return ModdedShaderCreator.createShader("sps", getSource("sps", extension.getProgramSet()), AlphaTests.ONE_TENTH_ALPHA,
                        DefaultVertexFormat.POSITION_TEX_COLOR, false, false, false, false, pipeline);
            }
        } catch (FakeChainedJsonException json) {
            throw json.getTrueException();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mekasuit", e);
        }
        return null;
    }

    private static ProgramSource getSource(String program, ProgramSet programSet) {
        try {
            var vertexOpt = Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("monocle", "shaders/" + program + ".vsh"));
            String vertexSource = null;
            if (vertexOpt.isPresent()) {
                vertexSource = vertexOpt.get().openAsReader().lines().collect(Collectors.joining(System.lineSeparator()));
            }

            var geometryOpt = Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("monocle", "shaders/" + program + ".gsh"));
            String geometrySource = null;
            if (geometryOpt.isPresent()) {
                geometrySource = geometryOpt.get().openAsReader().lines().collect(Collectors.joining(System.lineSeparator()));
            }

            var tessControlOpt = Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("monocle", "shaders/" + program + ".tcs"));
            String tessControlSource = null;
            if (tessControlOpt.isPresent()) {
                tessControlSource = tessControlOpt.get().openAsReader().lines().collect(Collectors.joining(System.lineSeparator()));
            }

            var tessEvalOpt = Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("monocle", "shaders/" + program + ".tes"));
            String tessEvalSource = null;
            if (tessEvalOpt.isPresent()) {
                tessEvalSource = tessEvalOpt.get().openAsReader().lines().collect(Collectors.joining(System.lineSeparator()));
            }
            var fragmentOpt = Minecraft.getInstance().getResourceManager().getResource(ResourceLocation.fromNamespaceAndPath("monocle", "shaders/" + program + ".fsh"));
            String fragmentSource = null;
            if (fragmentOpt.isPresent()) {
                fragmentSource = fragmentOpt.get().openAsReader().lines().collect(Collectors.joining(System.lineSeparator()));
            }
            ProgramSource source = new ProgramSource(program, vertexSource, geometrySource, tessControlSource, tessEvalSource, fragmentSource, programSet, ((ProgramSetExtension)programSet).getShaderProperties(), null);
            return source;
        }catch (Exception ignored) {

        }
        return null;
    }
}