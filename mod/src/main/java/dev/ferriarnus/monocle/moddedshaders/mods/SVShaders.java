package dev.ferriarnus.monocle.moddedshaders.mods;

import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;
import net.povstalec.stellarview.client.render.shader.StellarViewVertexFormat;

public class SVShaders {

    public static final ResourceLocation STAR = ResourceLocation.fromNamespaceAndPath("stellarview","rendertype_star");
    public static final ResourceLocation STAR_TEX = ResourceLocation.fromNamespaceAndPath("stellarview","rendertype_star_tex");
    public static final ResourceLocation DUST_CLOUD = ResourceLocation.fromNamespaceAndPath("stellarview","rendertype_dust_cloud");

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(STAR, AlphaTests.NON_ZERO_ALPHA, StellarViewVertexFormat.STAR_POS_COLOR_LY.get(), false, ProgramId.SkyTextured);
        ModdedShaderPipeline.addShaderFromJson(STAR_TEX, AlphaTests.NON_ZERO_ALPHA, StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX.get(), false, ProgramId.SkyTextured);
        ModdedShaderPipeline.addShaderFromJson(DUST_CLOUD, AlphaTests.NON_ZERO_ALPHA, StellarViewVertexFormat.STAR_POS_COLOR_LY_TEX.get(), false, ProgramId.SkyTextured);
    }
}
