package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;

public class CaxtonShaders {

    public static final ResourceLocation TEXT = ResourceLocation.fromNamespaceAndPath("caxton", "rendertype_text");
    public static final ResourceLocation TEXT_SEE_THROUGH = ResourceLocation.fromNamespaceAndPath("caxton", "rendertype_text_see_through");
    public static final ResourceLocation TEXT_OUTLINE = ResourceLocation.fromNamespaceAndPath("caxton", "rendertype_text_outline");

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(TEXT, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, true, ProgramId.BlockTrans);
        ModdedShaderPipeline.addShaderFromJson(TEXT_SEE_THROUGH, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, true, ProgramId.BlockTrans);
        ModdedShaderPipeline.addShaderFromJson(TEXT_OUTLINE, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, true, ProgramId.BlockTrans);
    }
}
