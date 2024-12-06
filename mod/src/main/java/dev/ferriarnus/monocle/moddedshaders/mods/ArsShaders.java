package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;

public class ArsShaders {

    public static ResourceLocation SKY = ResourceLocation.fromNamespaceAndPath("ars_nouveau","sky");
    public static ResourceLocation BLAMED_ENTITY = ResourceLocation.fromNamespaceAndPath("ars_nouveau","blamed_entity");
    public static ResourceLocation RAINBOW_ENTITY = ResourceLocation.fromNamespaceAndPath("ars_nouveau","rainbow_entity");

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(SKY, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION, false, ProgramId.SkyBasic);
        ModdedShaderPipeline.addShaderFromJson(BLAMED_ENTITY, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY, false, ProgramId.Entities);
        ModdedShaderPipeline.addShaderFromJson(RAINBOW_ENTITY, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY, false, ProgramId.Entities);
    }
}
