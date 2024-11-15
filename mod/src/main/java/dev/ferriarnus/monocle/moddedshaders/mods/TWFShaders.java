package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.irisCompatibility.impl.EmbeddiumShader;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.irisshaders.iris.shadows.ShadowRenderingState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class TWFShaders {

    public static ResourceLocation AURORA = ResourceLocation.fromNamespaceAndPath("twilightforest", "aurora/aurora");

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("twilightforest", "textures/entity/red_thread.png");

    public static final RenderType GLOW = RenderType
            .create("twilightforest" + ":glow", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, RenderType.CompositeState
                    .builder()
                    .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                    .setCullState(new RenderStateShard.CullStateShard(true))
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard("always", 519))
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> ((IrisRenderingPipeline) Iris.getPipelineManager().getPipelineNullable()).getShaderMap().getShader(ShadowRenderingState.areShadowsCurrentlyBeingRendered() ? ShaderKey.SHADOW_BASIC : ShaderKey.TERRAIN_SOLID)))
                    .setTextureState(new RenderStateShard.TextureStateShard(TEXTURE, false, true))
                    .createCompositeState(true));

    static {
        init();
    }

    public static void init() {
        ModdedShaderPipeline.addShaderFromJson(AURORA, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_COLOR, false, ProgramId.SkyTextured);
    }
}
