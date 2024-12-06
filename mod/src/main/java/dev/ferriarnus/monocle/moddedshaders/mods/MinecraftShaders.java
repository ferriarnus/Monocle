package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.resources.ResourceLocation;

public class MinecraftShaders {

    public static ResourceLocation END_PORTAL = ResourceLocation.parse("rendertype_end_portal");
    public static ResourceLocation END_GATEWAY = ResourceLocation.parse("rendertype_end_gateway");

    public static final RenderType END_PORTAL_TYPE = RenderType.create(
            "end_portal",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(END_PORTAL)))
                    .setTextureState(new RenderStateShard.TextureStateShard(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false))
                    .createCompositeState(false)
    );

    public static final RenderType END_GATEWAY_TYPE = RenderType.create(
            "end_gateway",
            DefaultVertexFormat.POSITION,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(END_GATEWAY)))
                    .setTextureState(new RenderStateShard.TextureStateShard(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false))
                    .createCompositeState(false)
    );

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(END_PORTAL, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION, false, ProgramId.Entities);
        ModdedShaderPipeline.addShaderFromJson(END_GATEWAY, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION, false, ProgramId.Entities);
    }
}
