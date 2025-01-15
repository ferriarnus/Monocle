package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;

public class EnderStorageShaders {
    public static final ResourceLocation STARFIELD = ResourceLocation.fromNamespaceAndPath("enderstorage", "starfield");

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(STARFIELD, AlphaTests.NON_ZERO_ALPHA, DefaultVertexFormat.POSITION, false, ProgramId.Entities);
    }
}
