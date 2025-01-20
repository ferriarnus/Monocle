package dev.ferriarnus.monocle.moddedshaders.mods;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import dev.ferriarnus.monocle.moddedshaders.ModdedShaderPipeline;
import net.irisshaders.iris.gl.blending.AlphaTests;
import net.irisshaders.iris.shaderpack.loading.ProgramId;
import net.minecraft.resources.ResourceLocation;

public class PingShaders {

    public static final ResourceLocation PING = ResourceLocation.fromNamespaceAndPath("ping", "rendertype_ping");

    static {
        init();
    }

    private static void init() {
        ModdedShaderPipeline.addShaderFromJson(PING, AlphaTests.ONE_TENTH_ALPHA, DefaultVertexFormat.POSITION_TEX_COLOR, false, ProgramId.EntitiesTrans);
    }
}
