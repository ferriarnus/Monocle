package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;

public class FAShaders {

    public static ResourceLocation FULLBRIGHT_CUTOUT = ResourceLocation.fromNamespaceAndPath("forbidden_arcanus", "rendertype_entity_fullbright_cutout");
    public static ResourceLocation FULLBRIGHT_TRANSLUCENT = ResourceLocation.fromNamespaceAndPath("forbidden_arcanus", "rendertype_entity_fullbright_translucent");

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(FULLBRIGHT_CUTOUT, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.NEW_ENTITY, true, ProgramId.Entities);
        ModdedShaderPipeline.addShaderFromJson(FULLBRIGHT_TRANSLUCENT, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.NEW_ENTITY, true, ProgramId.EntitiesTrans);
    }
}
