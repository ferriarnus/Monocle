package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;

public class COShaders {

    public static final ResourceLocation ENERGY_SWIRL_RENDERTYPE = ResourceLocation.fromNamespaceAndPath("creeperoverhaul", "rendertype_energy_swirl");

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(ENERGY_SWIRL_RENDERTYPE, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.NEW_ENTITY, true, ProgramId.EntitiesTrans);
    }
}
