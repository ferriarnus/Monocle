package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class XYShaders {

    public static final ResourceLocation LASER_NODE = ResourceLocation.fromNamespaceAndPath("xycraft_core", "laser_node");
    public static final ResourceLocation LASER = ResourceLocation.fromNamespaceAndPath("xycraft_core", "laser");
    public static final ResourceLocation ICOSPHERE = ResourceLocation.fromNamespaceAndPath("xycraft_core", "icosphere");

    static {
        init();
    }

    public static final Function<ResourceLocation, RenderType> LaserNode = Util.memoize(resourceLocation -> {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(LASER_NODE)))
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setCullState(RenderStateShard.NO_CULL)
                .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                .setLightmapState(RenderType.NO_LIGHTMAP)
                .setOverlayState(RenderType.NO_OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setOutputState(RenderStateShard.MAIN_TARGET)
                .createCompositeState(true);
        return RenderType.create("xycraft_laser_node", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, true, false, state);
    });

    public static final Function<ResourceLocation, RenderType> Laser = Util.memoize(resourceLocation -> {
        RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(LASER)))
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setCullState(RenderStateShard.NO_CULL)
                .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                .setLightmapState(RenderType.NO_LIGHTMAP)
                .setOverlayState(RenderType.NO_OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setOutputState(RenderStateShard.MAIN_TARGET)
                .createCompositeState(true);
        return RenderType.create("xycraft_laser",DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, true, false,rendertype$state);
    });

    public static final Function<ResourceLocation, RenderType> icosphere = Util.memoize(resourceLocation -> {
        RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(ICOSPHERE)))
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setCullState(RenderStateShard.CULL)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setLightmapState(RenderType.NO_LIGHTMAP)
                .setOverlayState(RenderType.NO_OVERLAY)
                .setOutputState(RenderStateShard.MAIN_TARGET)
                .createCompositeState(true);
        return RenderType.create("xycraft_icosphere", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, true, true, rendertype$state);
    });

    public static void init() {
        ModdedShaderPipeline.addShaderFromJson(LASER_NODE, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.BLOCK, false, ProgramId.EntitiesTrans);
        ModdedShaderPipeline.addShaderFromJson(LASER, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.BLOCK, false, ProgramId.EntitiesTrans);
        ModdedShaderPipeline.addShaderFromJson(ICOSPHERE, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.BLOCK, false, ProgramId.EntitiesTrans);
    }

}
