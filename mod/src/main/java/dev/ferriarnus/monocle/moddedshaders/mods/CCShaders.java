package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dan200.computercraft.client.render.text.FixedWidthFontRenderer;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class CCShaders {

    private static ResourceLocation TBO = ResourceLocation.parse("computercraft/monitor_tbo");

    private static final RenderStateShard.TextureStateShard TERM_FONT_TEXTURE = new RenderStateShard.TextureStateShard(
            FixedWidthFontRenderer.FONT, false, false);

    public static final RenderType MONITOR_TBO = RenderType.create(
            "monitor_tbo", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLE_STRIP, 128,
            false, false,
            RenderType.CompositeState.builder()
                    .setTextureState(TERM_FONT_TEXTURE)
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> ModdedShaderPipeline.getShader(TBO)))
                    .createCompositeState(false)
    );

    static {
        init();
    }

    public static void init() {
        ModdedShaderPipeline.addShaderFromJson(TBO, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX, false, ProgramId.Entities);
    }
}
