package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;

public class ESShaders {

    public static final ResourceLocation ECLIPSE = ResourceLocation.fromNamespaceAndPath("eternal_starlight", "rendertype_eclipse");
    public static final ResourceLocation STARLIGHT_PORTAL = ResourceLocation.fromNamespaceAndPath("eternal_starlight", "rendertype_starlight_portal");

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(ECLIPSE, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.BLOCK, false, ProgramId.BlockTrans);
        ModdedShaderPipeline.addShaderFromJson(STARLIGHT_PORTAL, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.BLOCK, false, ProgramId.BlockTrans);
    }
}
